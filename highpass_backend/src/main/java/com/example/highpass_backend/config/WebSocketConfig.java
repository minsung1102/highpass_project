package com.example.highpass_backend.config;

import com.example.highpass_backend.websocket.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final StompHandler stompHandler;

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompHandler);
    }
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/sub");
        registry.setApplicationDestinationPrefixes("/pub");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-stomp").setAllowedOriginPatterns("*").withSockJS();;
    }
}
// 주현님 저는 왜 커피 안사줘요????
// 저도 커피 좋아하는 데
// 저는 억울합니다.
// 저는 마지막 회식 안갑니다.
// 이유는 욕먹기 싫어서입니다.
// 저는 아이스 초코 사주세요
// 저는 아이스티 사주세요
// 현수막 중에 유치권 행사중 보셨나요??
// 근데 왜 초등권 행사나 중등권 행사는 왜 없죠?
// 재미있죠?
// 주현님행동 한번 했습니다.
// 쏘 이지
// 명원님 취했더라구요
// 저한테
// 아잉
// 부끄러><
// 명원님 저는 진짜 종강회식 안가니까\
// 명원님이 분위기 메이커 해주세요
// 저처럼 해주세요
// 그게 인생이고
// 그게 주님의 뜻입니다.
// 뭐라는거야는
// 반말이에요
// 혼잣말을 그렇게 크게하나요?
// 내맘이 아니라 제 맘이죠
// 이게 존댓말이에요
// 알겠죠???
/*
저는 한타 650타
영타 300타
보유중인
사람입니다.
거의 속기사급이죠??
참고로 저는 3팀 팀장이에여
저를 믿으십시오.
그게 인생입니다.
*/