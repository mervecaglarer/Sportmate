package ceng.gui.sportmate.model;

public class TopicItem {

    String title, content, date, userPhoto;

    public TopicItem() {

    }

    public TopicItem(String title, String content, String date, String userPhoto) {
        this.title = title;
        this.content = content;
        this.date = date;
        this.userPhoto = userPhoto;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getDate() {
        return date;
    }

    public String getUserPhoto() {
        return userPhoto;
    }
}
