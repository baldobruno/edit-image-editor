package com.bruno.image_editor.controller;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Shape;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.imageio.ImageIO;

import org.apache.logging.slf4j.SLF4JLogger;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ImageEditorController {

	private static Logger logger = LoggerFactory.getLogger(ImageEditorController.class);

	@GetMapping("/add-text-to-image")
	public ResponseEntity<Resource> addTextToImage(@RequestParam(value = "text", required = true) String text)
			throws IOException {
		logger.error("riga 43" + text);
		InputStream initialStream = new ClassPathResource("/static/Office21.jpg").getInputStream();
		logger.error("riga 45" + initialStream);
		File targetFile = File.createTempFile("ciao", ".jpg");
		logger.error("riga 48 - il file esiste???" + targetFile.exists());
		Files.copy(initialStream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		logger.error("riga 50 - il file esiste???" + targetFile.exists());
		IOUtils.closeQuietly(initialStream);
		logger.info("riga 52" + targetFile.getAbsolutePath());
		BufferedImage image = ImageIO.read(targetFile);
		Font font = new Font("Arial", Font.BOLD, 100);
		Graphics g = image.getGraphics();

		FontMetrics ruler = g.getFontMetrics(font);
		GlyphVector vector = font.createGlyphVector(ruler.getFontRenderContext(), text);

		Shape outline = vector.getOutline(0, 0);

		double expectedWidth = outline.getBounds().getWidth();
		double expectedHeight = outline.getBounds().getHeight();

		boolean textFits = image.getWidth() >= expectedWidth && image.getHeight() >= expectedHeight;
		// If text does not fit image size reduce the text font size
		if (!textFits) {
			double widthBasedFontSize = (font.getSize2D() * image.getWidth()) / expectedWidth;
			double heightBasedFontSize = (font.getSize2D() * image.getHeight()) / expectedHeight;

			double newFontSize = widthBasedFontSize < heightBasedFontSize ? widthBasedFontSize : heightBasedFontSize;
			font = font.deriveFont(font.getStyle(), (float) newFontSize - 5);
		}

		FontMetrics metrics = g.getFontMetrics(font);
		// Insert the text at the center of the image
		int positionX = (image.getWidth() - metrics.stringWidth(text)) / 2;
		int positionY = (image.getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
		g.setFont(font);
		g.setColor(Color.RED);
		g.drawString(text, positionX, positionY);

		// Download the image with added text as a jpeg
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(image, "jpg", baos);
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentDisposition(ContentDisposition.attachment().build());
		return ResponseEntity.ok().headers(httpHeaders).contentType(MediaType.IMAGE_JPEG)
				.body(new ByteArrayResource(baos.toByteArray()));
	}
}
