package io.hayk.demo.resources.note.content;


import io.hayk.demo.note.content.*;
import io.hayk.demo.note.user.GetUserIdByExternalAccountUidRequest;
import io.hayk.demo.note.user.GetUserIdByExternalAccountUidResult;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RestController
@RequestMapping("/api/note")
public class NoteController {

    private final Mono<RSocketRequester> notesApiRequester;

    public NoteController(final Mono<RSocketRequester> notesApiRequester) {
        this.notesApiRequester = notesApiRequester;
    }

    @PostMapping("/create")
    public Mono<GenericNoteResult> create(final Mono<CreateNoteWebRequest> createNoteRequest, final Mono<Principal> principal) {
        return userId(principal).flatMap(userId ->
                createNoteRequest
                        .flatMap(request -> notesApiRequester.flatMap(
                                rSocketRequester ->
                                        rSocketRequester.route("note:create")
                                                .data(createNoteRequest(request, userId))
                                                .retrieveMono(GenericNoteResult.class))

                        ));
    }

    private static CreateNoteRequest createNoteRequest(final CreateNoteWebRequest webRequest, final Long userId) {
        final CreateNoteRequest request = new CreateNoteRequest();
        request.setText(webRequest.getText());
        request.setTitle(webRequest.getTitle());
        request.setUserId(userId);
        return request;
    }

    @PutMapping("/update")
    public Mono<GenericNoteResult> update(final Mono<UpdateNoteWebRequest> updateNoteRequest, final Mono<Principal> principal) {
        return userId(principal).flatMap(userId ->
                updateNoteRequest
                        .flatMap(request -> notesApiRequester.flatMap(
                                rSocketRequester ->
                                        rSocketRequester.route("note:update")
                                                .data(updateNoteRequest(request, userId))
                                                .retrieveMono(GenericNoteResult.class))

                        ));
    }

    private static UpdateNoteRequest updateNoteRequest(final UpdateNoteWebRequest webRequest, final Long userId) {
        final UpdateNoteRequest request = new UpdateNoteRequest();
        request.setText(webRequest.getText());
        request.setTitle(webRequest.getTitle());
        request.setUserId(userId);
        request.setNoteId(webRequest.getNoteId());
        return request;
    }

    @GetMapping("{id}")
    public Mono<GenericNoteResult> getNote(@PathVariable("id") final Long id) {
        return notesApiRequester.flatMap(
                rSocketRequester ->
                        rSocketRequester.route("note:get")
                                .data(new GetNoteRequest(id))
                                .retrieveMono(GenericNoteResult.class)

        );
    }

    @GetMapping("/all")
    public Mono<GenericNoteResult> getUserNotes(@RequestParam("user_id") final Mono<Principal> principal) {
        return userId(principal).flatMap(userId ->
                notesApiRequester.flatMap(
                        rSocketRequester ->
                                rSocketRequester.route("note:userNotes")
                                        .data(new GetUserNotesRequest(userId))
                                        .retrieveMono(GenericNoteResult.class)

                )
        );
    }

    private Mono<Long> userId(final Mono<Principal> principal) {
        return notesApiRequester.flatMap(rSocketRequester ->
                principal.map(OAuth2AuthenticationToken.class::cast)
                        .map(OAuth2AuthenticationToken::getPrincipal)
                        .map(OAuth2User::getAttributes)
                        .map(attributes -> attributes.get("sub"))
                        .map(String::valueOf)
                        .flatMap(externalAccountUId ->
                                rSocketRequester.route("user:id")
                                        .data(new GetUserIdByExternalAccountUidRequest(externalAccountUId))
                                        .retrieveMono(GetUserIdByExternalAccountUidResult.class))
        ).map(GetUserIdByExternalAccountUidResult::getUserId);
    }
}
