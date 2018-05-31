package classes;

/**
 * Created by 11ryt on 7/13/2017.
 */
public class AttendanceCode {

    private String code;

    public AttendanceCode(String attendanceCode) {
        code = attendanceCode;
    }

    public boolean equals(String s){
        return this.toString().equals(s);
    }
    public boolean equals(AttendanceCode ac){
        return this.toString().equals(ac.toString());
    }
    public String toString(){
        return code;
    }
}
