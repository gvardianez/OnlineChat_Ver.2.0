package chat_server.errors;

public class AlreadyAuthorizeException extends  RuntimeException{
    public AlreadyAuthorizeException(String message){
        super(message);
    }
}
