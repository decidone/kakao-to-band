package com.decidone.messenger;

public class PostItem {

    private String access_token;
    private String band_key;
    private String content;
    private boolean do_push;
    private int result_code;

    public int getResult_code() {
        return result_code;
    }

    public void setResult_code(int result_code) {
        this.result_code = result_code;
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getBand_key() {
        return band_key;
    }

    public void setBand_key(String band_key) {
        this.band_key = band_key;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isDo_push() {
        return do_push;
    }

    public void setDo_push(boolean do_push) {
        this.do_push = do_push;
    }
}
