package kr.chat.controller;



import kr.chat.document.ChatRoom;
import kr.chat.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@RestController
@RequiredArgsConstructor
@CrossOrigin("*")
@RequestMapping("/api/chatRoom")
public class ChatRoomController {
    private final ChatRoomService chatRoomService;


    @PostMapping("/save")
    public Mono<ChatRoom> save(@RequestBody ChatRoom chatRoom) {
        return chatRoomService.save(chatRoom);
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
        return chatRoomService.findById(id);
    }



    @DeleteMapping("/deleteById/{id}")
    public Mono<Void> deleteById(@PathVariable String id) {
        return chatRoomService.existsById(id)
                .flatMap(exists -> {
                    if (exists) {
                        return chatRoomService.deleteById(id);
                    } else {
                        return Mono.empty(); // 채팅방이 없는 경우 빈 Mono 반환
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