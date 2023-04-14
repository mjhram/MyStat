package mjhram.com.mystat;

public class user_info {
    long     id;
    String  name;
    float   storedReading;
    String  storedNote;
    public user_info() {
        this.storedReading = 50f;
        this.storedNote = "";
    }

    @Override
    public String toString() {
        return name;
    }
}
