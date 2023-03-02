package mjhram.com.mystat;

public class user_info {
    long     id;
    String  name;
    float   storedReading;

    public user_info() {
        this.storedReading = 50f;
    }

    @Override
    public String toString() {
        return name;
    }
}
