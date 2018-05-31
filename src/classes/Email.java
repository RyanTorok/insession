package classes;

/**
 * Created by S507098 on 4/27/2017.
 */
public class Email {
    private String address;
    private String host;

    public Email(String s){
        int ind = s.indexOf("@");
        address = s.substring(0, ind);
        host = s.substring(ind+1);
    }
    public String getEmail(){
        return address+ "@" + host;
    }

}
