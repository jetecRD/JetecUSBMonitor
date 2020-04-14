package com.jetec.usbmonitor.Model.EventBusModel;

public class ImageEvent {
    private byte[] image;

    public ImageEvent() {

    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }
}
