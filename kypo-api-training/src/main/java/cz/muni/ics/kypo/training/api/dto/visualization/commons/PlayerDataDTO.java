package cz.muni.ics.kypo.training.api.dto.visualization.commons;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import lombok.*;

@EqualsAndHashCode
@Getter
@Setter
@ToString
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

    public void setPicture(byte[] picture) {
        this.picture = picture;
        this.avatarColor = extractAvatarColor(picture);
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
}
