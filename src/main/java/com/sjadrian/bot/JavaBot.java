package com.sjadrian.bot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.games.Animation;
import org.telegram.telegrambots.meta.api.objects.stickers.Sticker;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.util.*;
import java.util.concurrent.*;
import java.io.FileReader;

// main
public class JavaBot extends TelegramLongPollingBot {

    private static final Stack<Long> allSearchingID = new Stack<>();
    private final List<Session> sessions = new ArrayList<>();
    private final Object lock = new Object();
    private final Properties properties = new Properties();
    private String BOT_TOKEN;
    private String BOT_USERNAME;

    public JavaBot() {
        getBotData();
        this.BOT_USERNAME =  properties.getProperty("BOT_USERNAME");
        this.BOT_TOKEN =  properties.getProperty("BOT_TOKEN");
    }

    private void getBotData() {
        try (FileReader reader = new FileReader("config")) {
            properties.load(reader);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN ;
    }

    @Override
    public void onUpdateReceived(Update update) {
        // get senderID
        long myId = update.getMessage().getChatId();

        // get partnerID
        Long partnerId = null;
        for (Session session : sessions) {
            if (session.getPartner(myId) != null) {
                partnerId = session.getPartner(myId);
            }
        }

        // check if the update has a message and the message has text
        if (update.hasMessage() && update.getMessage().hasText()) {

            // get message
            String sentText = update.getMessage().getText();

            if (sentText.equals("/search") && partnerId != null) {
                sendMessageHasPartner(myId);
            } else if (sentText.equals("/search") && !allSearchingID.contains(myId)) {
                searching(myId);
            } else if (sentText.equals("/search") && allSearchingID.contains(myId)) {
                sendMessageStillSearching(myId);
            } else if (sentText.equals("/stop")) {
                stop(myId);
            } else if (sentText.equals("/next")) {
                next(myId);
            } else {
                sendMessage(sentText, partnerId);
            }
        }

        if (update.hasMessage() && update.getMessage().hasPhoto()) {
            List<PhotoSize> photos = update.getMessage().getPhoto();

            String fileID = photos.stream()
                    .sorted(Comparator.comparing(PhotoSize::getFileSize).reversed())
                    .findFirst()
                    .orElse(null)
                    .getFileId();

            sendPhoto(fileID, partnerId);
        }

        if (update.hasMessage() && update.getMessage().hasAnimation()) {
            Animation animation = update.getMessage().getAnimation();
            SendAnimation sendAnimation = new SendAnimation();

            if (partnerId != null) {
                sendAnimation.setChatId(partnerId);
                sendAnimation.setAnimation(new InputFile(animation.getFileId()));

                try {
                    execute(sendAnimation);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }

        if (update.hasMessage() && update.getMessage().hasSticker()) {
            Sticker sticker = update.getMessage().getSticker();
            SendSticker sendSticker = new SendSticker();

            if (partnerId != null) {
                sendSticker.setChatId(partnerId);
                sendSticker.setSticker(new InputFile(sticker.getFileId()));

                try {
                    execute(sendSticker);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }

        if (update.hasMessage() && update.getMessage().hasAudio()) {
            Audio audio = update.getMessage().getAudio();
            SendAudio sendAudio = new SendAudio();

            if (partnerId != null) {
                sendAudio.setChatId(partnerId);
                sendAudio.setAudio(new InputFile(audio.getFileId()));

                try {
                    execute(sendAudio);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }

        if (update.hasMessage() && update.getMessage().hasVideo()) {
            Video video = update.getMessage().getVideo();
            SendVideo sendVideo = new SendVideo();

            if (partnerId != null) {
                sendVideo.setChatId(partnerId);
                sendVideo.setVideo(new InputFile(video.getFileId()));

                try {
                    execute(sendVideo);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }

        if (update.hasMessage() && update.getMessage().hasContact()) {
            Contact contact = update.getMessage().getContact();
            SendContact sendContact = new SendContact();

            if (partnerId != null) {

                sendContact.setChatId(partnerId);
                sendContact.setPhoneNumber(contact.getPhoneNumber());
                sendContact.setFirstName(contact.getFirstName());
                sendContact.setFirstName(contact.getLastName());

                try {
                    execute(sendContact);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }

        if (update.hasMessage() && update.getMessage().hasDocument()) {
            Document document = update.getMessage().getDocument();
            SendDocument sendDocument = new SendDocument();


            if (partnerId != null)
                sendDocument.setChatId(partnerId);
            sendDocument.setDocument(new InputFile(document.getFileId()));

            try {
                execute(sendDocument);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }

        if (update.hasMessage() && update.getMessage().hasVoice()) {

            Voice voice = update.getMessage().getVoice();
            SendVoice sendVoice = new SendVoice();

            if (partnerId != null)
                sendVoice.setChatId(partnerId);
            sendVoice.setVoice(new InputFile(voice.getFileId()));

            try {
                execute(sendVoice);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }

        if (update.hasMessage() && update.getMessage().hasVideoNote()) {

            VideoNote videoNote = update.getMessage().getVideoNote();
            SendVideoNote sendVideoNote = new SendVideoNote();

            if (partnerId != null)
                sendVideoNote.setChatId(partnerId);
            sendVideoNote.setVideoNote(new InputFile(videoNote.getFileId()));

            try {
                execute(sendVideoNote);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }

        if (update.hasMessage() && update.getMessage().hasLocation()) {

            Location location = update.getMessage().getLocation();
            SendLocation sendLocation = new SendLocation();

            if (partnerId != null)
                sendLocation.setChatId(partnerId);
            sendLocation.setLongitude(location.getLongitude());
            sendLocation.setLatitude(location.getLatitude());

            try {
                execute(sendLocation);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    private void searching(Long myId) {
        sendMessageInitialSearching(myId);

        // Find partner
        CompletableFuture<Long> future = new CompletableFuture<>();
        future.completeAsync(() -> {
            findId(myId);

            Session matchedSession = sessions.stream().filter(session -> session.getPartner(myId) != null).findFirst().orElse(null);
            return matchedSession != null ? matchedSession.getPartner(myId) : null;
        });

        // Process result of find partner
        future.whenComplete((result, ex) -> {
            sendMessageSearchResponse(result != null, myId);
        });
    }

    private void stop(Long myId) {
        // stop chat with current partner
        Session matchedSession = null;
        Long partnerId = null;

        for (Session session: sessions) {
            if (session.getPartner(myId) != null) {
                matchedSession = session;
                partnerId = session.getPartner(myId);
            }
        }
        sessions.remove(matchedSession);
        sendMessageStop(myId, partnerId, matchedSession != null);
    }

    private void next(long myId) {
        // stop chat with current partner and search for a new partner
        stop(myId);
        searching(myId);
    }

    private void sendMessage(String text, Long recipientID) {
        SendMessage message = new SendMessage();
        message.setText(text);
        if (recipientID != null) {
            message.setChatId(recipientID);
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendMessageHasPartner(long myId) {
        String text = "Currently you have a partner \n /stop - stop this conversation";
        sendMessage(text, myId);
    }

    private void sendMessageStillSearching(long myId) {
        String text = "Currently Searching is still in process please wait";
        sendMessage(text, myId);
    }

    private void sendMessageInitialSearching(Long myId) {
        String text = "Looking for a new chat...";
        sendMessage(text, myId);
    }

    private void sendMessageStop(Long myId, Long partnerId, Boolean connected) {
        if (connected) {
            String message_self = "You stopped the chat\nType /search to find a new partner";
            sendMessage(message_self, myId);

            String message_partner = "Your partner has stopped the chat\nType /search to find a new partner";
            sendMessage(message_partner, partnerId);

        } else {
            String message_text = "You currently do not have a partner\nType /search to find a new partner";
            sendMessage(message_text, myId);
        }
    }

    private void sendMessageSearchResponse(boolean b, Long myId) {
        String message_text = null;

        if (b) {
            message_text = "Partner found \n/next - find a new partner \n/stop - stop this chat";
            sendMessage(message_text , myId);

        } else {
            message_text = "Couldn't find a partner, please try again later";
            sendMessage(message_text , myId);
        }
    }

    private void sendPhoto(String fileID, Long recipientID) {
        SendPhoto sendPhoto = new SendPhoto();

        if (recipientID != null) {
            sendPhoto.setChatId(recipientID);
            sendPhoto.setPhoto(new InputFile(fileID));

            try {
                execute(sendPhoto);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    public void findId(Long myId) {
        Long partnerId;

        System.out.println("Currently Searching myId: " + myId);

        if (!allSearchingID.isEmpty()) {
//                partnerId = allSearchingID.remove(0);
            partnerId = allSearchingID.pop();

            synchronized (lock) {
                sessions.add(new Session(myId, partnerId));
            }

            System.out.println("Search Ends. Match Found. myId: " + myId + " partnerId: " + partnerId);
            return;
        }

        allSearchingID.add(myId);

        long t = System.currentTimeMillis();
        long end = t + 3000; // search max time = 3s

        while (System.currentTimeMillis() < end) {

            System.out.println("myId: " + myId + " Size sessions: " + sessions.size());

            Session matchedSession;

            synchronized (lock) {
                matchedSession = sessions.stream()
                        .filter(x -> x.getPartner(myId) != null)
                        .findFirst()
                        .orElse(null);
            }

            if (matchedSession != null) {
                allSearchingID.remove(myId);
                System.out.println("Search Ends. Match Found. myId: " + myId + " partnerId: " + matchedSession.getPartner(myId));
                return;
            }

            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        allSearchingID.remove(myId);
        System.out.println("Search Ends. Match Not Found. myId: " + myId);
    }
}
