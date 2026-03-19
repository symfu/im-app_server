package cn.wildfirechat.app.avatar;

import org.springframework.core.io.ClassPathResource;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

public class NameAvatarBuilder {

    private BufferedImage templateImage;
    private Graphics2D templateG2D;
    private int templateWidth;
    private int templateHeight;

    private String fullName;

    private static volatile Font font;

    public NameAvatarBuilder(String bgRGB) {
        templateImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        templateG2D = templateImage.createGraphics();
        templateWidth = templateImage.getWidth();
        templateHeight = templateImage.getHeight();
        templateG2D.setBackground(Color.decode(bgRGB));
        templateG2D.clearRect(0, 0, templateWidth, templateHeight);
    }

    public NameAvatarBuilder name(String drawName, String fullName) {
        this.fullName = fullName;
        // Get the FontMetrics
        // 加载自定义字体
        if (font == null) {
            synchronized (NameAvatarBuilder.class) {
                if (font == null) {
                    Font loadFont = null;
                    try (InputStream inputStream = new ClassPathResource("fonts/simhei.ttf").getInputStream()) {
                        // 加载自定义字体
                        Font customFont = Font.createFont(Font.TRUETYPE_FONT, inputStream);
                        // 设置字体样式
                        loadFont = customFont.deriveFont(Font.PLAIN, 40);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    // 如果自定义字体加载失败，使用系统默认字体
                    if (loadFont == null) {
                        loadFont = new Font("SimHei", Font.PLAIN, 40);
                        // 如果系统没有黑体，再尝试其他中文字体
                        if (!loadFont.getFamily().equals("SimHei")) {
                            loadFont = new Font("Microsoft YaHei", Font.PLAIN, 40);
                        }
                        // 如果都不行，使用 SansSerif
                        if (loadFont.getFamily().equals("Dialog")) {
                            loadFont = new Font("SansSerif", Font.PLAIN, 40);
                        }
                    }
                    font = loadFont;
                }
            }
        }

        // 最终保护：如果字体仍为null，使用系统默认字体
        Font useFont = font != null ? font : new Font("SansSerif", Font.PLAIN, 40);
        FontMetrics metrics = templateG2D.getFontMetrics(useFont);
        // Determine the X coordinate for the text
        int x = (templateWidth - metrics.stringWidth(drawName)) / 2;
        // Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
        int y = ((templateHeight - metrics.getHeight()) / 2) + metrics.getAscent();
        // Set the font
        templateG2D.setFont(useFont);
        // Draw the String
        templateG2D.drawString(drawName, x, y);
        return this;
    }

    public File build() {
        templateG2D.dispose();
        templateImage.flush();
        File file = new File(AvatarServiceImpl.AVATAR_DIR, this.fullName.hashCode() + ".png");
        try {
            ImageIO.write(templateImage, "png", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // System.gc();
        return file;
    }
}