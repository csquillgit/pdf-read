package pdf.read;

import java.io.File;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class PdfReadMain {

	public static void main(String args[]) {
		new PdfReadMain().process();
	}

	private void process() {

		try {

			File file = new File("pdfs/sample.pdf");

			PDDocument document = PDDocument.load(file);

			PDFTextStripper pdfStripper = new PDFTextStripper();

			String text = pdfStripper.getText(document);

			System.out.println(text);

			document.close();

		} catch (Exception e) {

			e.printStackTrace();
		}

	}
}
