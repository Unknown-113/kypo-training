package cz.muni.ics.kypo.training.api.dto.visualization.commons;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Objects;

public abstract class PlayerDataDTO {

    private Long id;
    private String name;
    private byte[] picture;
    private String avatarColor;
    private Long trainingRunId;
    private long trainingTime;

    public PlayerDataDTO(Long id, String name, byte[] picture, Long trainingRunId) {
        this.id = id;
        this.name = name;
        this.picture = picture;
        this.avatarColor = extractAvatarColor(picture);
        this.trainingRunId = trainingRunId;
    }

    public PlayerDataDTO(Long id, String name, byte[] picture, Long trainingRunId, long trainingTime) {
        this(id, name, picture, trainingRunId);
        this.trainingTime = trainingTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTrainingRunId() {
        return trainingRunId;
    }

    public void setTrainingRunId(Long trainingRunId) {
        this.trainingRunId = trainingRunId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getPicture() {
        return picture;
    }

    public void setPicture(byte[] picture) {
        this.picture = picture;
        this.avatarColor = extractAvatarColor(picture);
    }

    public String getAvatarColor() {
        return avatarColor;
    }

    public void setAvatarColor(String avatarColor) {
        this.avatarColor = avatarColor;
    }

    public long getTrainingTime() {
        return trainingTime;
    }

    public void setTrainingTime(long trainingTime) {
        this.trainingTime = trainingTime;
    }

    private String extractAvatarColor(byte[] picture) {
        try {
            BufferedImage finalImage = ImageIO.read(new ByteArrayInputStream(picture));
            for(int y = 0; y < finalImage.getHeight(); y++) {
                for(int x = 0; x < finalImage.getWidth(); x++) {
                    int clr = finalImage.getRGB(x, y);
                    int red = (clr & 0x00ff0000) >> 16;
                    int green = (clr & 0x0000ff00) >> 8;
                    int blue = clr & 0x000000ff;
                    if (red != 255 && green != 255 && blue != 255) {
                        return String.format("#%02X%02X%02X", red, green, blue);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return String.format("#%02X%02X%02X", 255, 255, 255);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerDataDTO that = (PlayerDataDTO) o;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getTrainingRunId(), that.getTrainingRunId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getTrainingRunId());
    }

    @Override
    public String toString() {
        return "PlayerDataDTO{" +
                "id=" + id +
                ", trainingRunId=" + trainingRunId +
                ", name='" + name + '\'' +
                ", trainingTime=" + trainingTime +
                '}';
    }
}
