package chat_server.errors;

public class BadRequestException extends RuntimeException{
    public BadRequestException(String message){
        super(message);
    }
}
