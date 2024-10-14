package kr.chat.controller;


import com.amazonaws.services.kms.model.NotFoundException;
import kr.chat.document.ChatRoom;
import kr.chat.service.ChatRoomService;
import kr.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

//Post, Put Delete  Get

@RestController
@RequiredArgsConstructor
@CrossOrigin("*")
@RequestMapping("/api/chatRoom")
public class ChatRoomController {
    private final ChatRoomService chatRoomService;
    private final ChatService chatService;


    @PostMapping("/save")
    public Mono<ChatRoom> save(@RequestBody ChatRoom chatRoom) {
        return chatRoomService.save(chatRoom)
                .switchIfEmpty(Mono.error(new BadRequestException("Failed to save chat room"))); // 저장 실패 시 예외 처리
    }



    @GetMapping("/findAll/{nickname}")
    public Flux<ChatRoom> findAll(@PathVariable String nickname) {

        // Service 레이어로 nickname 전달
        return chatRoomService.findAllByNickname(nickname);
    }

    @PutMapping("/{id}")
    public Mono<ChatRoom> updateChatRoom(@PathVariable String id, @RequestBody ChatRoom chatRoom) {
        return chatRoomService.updateChatRoom(id, chatRoom);
    }

    @GetMapping("/{id}")
    public Mono<ChatRoom> findById(@PathVariable String id) {
        return chatRoomService.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Chat room not found"))); // 채팅방이 없는 경우 예외 처리
    }



    @DeleteMapping("/deleteById/{id}")
    public Mono<Void> deleteById(@PathVariable String id) {
        return chatRoomService.existsById(id)
                .flatMap(exists -> {
                    if (exists) {
                        return chatRoomService.deleteById(id);
                    } else {
                        return Mono.error(new NotFoundException("Chat room not found")); // 채팅방이 없는 경우 예외 처리
                    }
                });
    }



    @GetMapping("/existsById/{id}")
    public Mono<Boolean> existsById(@PathVariable String id) {
        return chatRoomService.existsById(id);
    }


    @GetMapping("/count")
    public Mono<Long> count() {
        return chatRoomService.count();
    }


}