package pdf.read;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

public class PDFTextLocator extends PDFTextStripper {

	private static String key_string;
	private static float x;
	private static float y;

	public PDFTextLocator() throws IOException {
		x = -1;
		y = -1;
	}

	/**
	 * Takes in a PDF Document, phrase to find, and page to search and returns the
	 * x,y in float array
	 * 
	 * @param document
	 * @param phrase
	 * @param page
	 * @return
	 * @throws IOException
	 */
	public static float[] getCoordiantes(PDDocument document, String phrase, int page) throws IOException {
		key_string = phrase;
		PDFTextStripper stripper = new PDFTextLocator();
		stripper.setSortByPosition(true);
		stripper.setStartPage(page + 1);
		stripper.setEndPage(page + 1);
		stripper.writeText(document, new OutputStreamWriter(new ByteArrayOutputStream()));
		y = document.getPage(page).getMediaBox().getHeight() - y;

		return new float[] { x, y };
	}

	/**
	 * Override the default functionality of PDFTextStripper.writeString()
	 */
	@Override
	protected void writeString(String string, List<TextPosition> textPositions) throws IOException {
		if (string.contains(key_string)) {
			TextPosition text = textPositions.get(0);
			if (x == -1) {
				x = text.getXDirAdj();
				y = text.getYDirAdj();
			}
		}
	}
}