package com.vc.socials.service;

import com.vc.socials.dto.NotificationDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {
    private final KafkaTemplate<String, NotificationDto> kafkaTemplate;

    @Autowired
    public KafkaProducerService(KafkaTemplate<String, NotificationDto> kafkaTemplate){
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendNotification(NotificationDto notification){
        kafkaTemplate.send("socials_intern", notification);
    }
}
