package translation.calltranslate;

import android.graphics.Bitmap;

/**
 * Created by Trinity Tuts on 10-01-2015.
 */
public class SelectUser {
    String name;
    Bitmap thumb;
    String phone;
    Boolean checkedBox = false;

    public Bitmap getThumb() {
        return thumb;
    }

    public void setThumb(Bitmap thumb) {
        this.thumb = thumb;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Boolean getCheckedBox() {
        return checkedBox;
    }

    public void setCheckedBox(Boolean checkedBox) {
        this.checkedBox = checkedBox;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}