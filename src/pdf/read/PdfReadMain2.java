package pdf.read;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

public class PdfReadMain2 {

	public static void main(String args[]) {
		new PdfReadMain2().process();
	}

	private void process() {

		try {

			File file = new File("pdfs/GLRP04R_PFULLER_GLBT01C1_062831_2.pdf");

			PDDocument document = PDDocument.load(file);

			// searchReplace("PFULLER", "CHUCK", "UTF-8", false, document);

			addToDoc(document);

			findHeaders();

//			List<COSObject> list = document.getDocument().getObjects();
//
//			for (COSObject cosObject : list) {
//				System.out.println(cosObject.getObject().toString());
//			}
//
//			PDFTextStripper pdfStripper = new PDFTextStripper();
//
//			pdfStripper.setSortByPosition(true);
//
//			String text = pdfStripper.getText(document);
//
//			System.out.println(text);

			document.close();

		} catch (Exception e) {

			e.printStackTrace();
		}

	}

	private static void searchReplace(String search, String replace, String encoding, boolean replaceAll,
			PDDocument doc) throws IOException {

		PDPageTree pages = doc.getDocumentCatalog().getPages();
		for (PDPage page : pages) {
			PDFStreamParser parser = new PDFStreamParser(page);
			parser.parse();
			List tokens = parser.getTokens();
			tokens.add(0, new COSString("What????"));
			// tokens.add(0, PDFObjectStreamParser("What????"));
			for (int j = 0; j < tokens.size(); j++) {
				Object next = tokens.get(j);
				System.out.println(next);
				if (next instanceof Operator) {
					Operator op = (Operator) next;
					// Tj and TJ are the two operators that display strings in a PDF
					// Tj takes one operator and that is the string to display so lets update that
					// operator
					if (op.getName().equals("Tj")) {
						COSString previous = (COSString) tokens.get(j - 1);
						String string = previous.getString();
						System.out.println("1:" + string);
						if (replaceAll)
							string = string.replaceAll(search, replace);
						else
							string = string.replaceFirst(search, replace);
						previous.setValue(string.getBytes());
					} else if (op.getName().equals("TJ")) {
						COSArray previous = (COSArray) tokens.get(j - 1);
						for (int k = 0; k < previous.size(); k++) {
							Object arrElement = previous.getObject(k);
							if (arrElement instanceof COSString) {
								COSString cosString = (COSString) arrElement;
								String string = cosString.getString();
								System.out.println("2:" + string);
								if (replaceAll)
									string = string.replaceAll(search, replace);
								else
									string = string.replaceFirst(search, replace);
								cosString.setValue(string.getBytes());
							}
						}
					}
				}
			}
			// now that the tokens are updated we will replace the page content stream.
//			PDStream updatedStream = new PDStream(doc);
//			OutputStream out = updatedStream.createOutputStream();
//			ContentStreamWriter tokenWriter = new ContentStreamWriter(out);
//			tokenWriter.writeTokens(tokens);
//			out.close();
//			page.setContents(updatedStream);

			PDStream updatedStream = new PDStream(doc);
			OutputStream out = updatedStream.createOutputStream();
			// FileOutputStream fos = new FileOutputStream("chuck.pdf");
			ContentStreamWriter tokenWriter = new ContentStreamWriter(out);
			tokenWriter.writeTokens(tokens);
			// tokenWriter.writeToken(new PDTe("What????"));

			out.close();
			page.setContents(updatedStream);
		}

		doc.save("pdfs/chuck1.pdf");
		// doc.close();
	}

	public void addToDoc(PDDocument x) {

		try {

			File file = new File("pdfs/GLRP04R_PFULLER_GLBT01C1_062831_2.pdf");

			PDDocument document = PDDocument.load(file);

			PDPage page = (PDPage) document.getDocumentCatalog().getPages().get(0);

			PDPageContentStream cos = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.PREPEND,
					true);

			PDFont fontPlain = PDType1Font.COURIER;
			// System.out.println("Font height:" + fontPlain.getBoundingBox().getHeight());
			PDRectangle rect = page.getMediaBox();

			// System.out.println(rect.getWidth() + ":" + rect.getHeight());

			// Define a text content stream using the selected font, move the cursor and

			// draw some text

			// cos.beginText();
			cos.setFont(fontPlain, 8);
			// cos.newLineAtOffset(0, 660);
			// System.out.println(rect.getHeight() - (6 * 8));
			// cos.newLineAtOffset(0, 672);
			// cos.showText("<-------------------------------->");
			// cos.drawLine(0, 672, 168, 672);
			cos.moveTo(0, 672);
			cos.lineTo(168, 672);
			cos.stroke();
			// cos.endText();

//			cos.beginText();
//			cos.setFont(fontPlain, 8);
//			System.out.println(rect.getHeight() - (7 * 8));
//			cos.newLineAtOffset(0, rect.getHeight() - (7 * 8));
//			// cos.newLineAtOffset(0, 671);
//			// cos.showText("<-------------------------------->");
//			cos.endText();

			cos.close();

			document.save("pdfs/chuck2.pdf");

			document.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void findHeaders() {

		try {

			File file = new File("pdfs/GLRP04R_PFULLER_GLBT01C1_062831_2.pdf");

			PDDocument document = PDDocument.load(file);

			// PDPage page = (PDPage) document.getDocumentCatalog().getPages().get(0);

			float[] xs = PDFTextLocator.getCoordiantes(document, "<------", 1);

			for (float f : xs) {
				System.out.println(f);
			}

//			PDFTextStripper pdfStripper = new PDFTextStripper();
//
//			pdfStripper.setSortByPosition(true);
//
//			String text = pdfStripper.getText(document);
//
//			String[] lines = text.split("\n");
//
//			for (int i = 0; i < lines.length; i++) {
//				String line = lines[i];
//
//				// if (line.startsWith("<----")) {
//				System.out.println(i + ":" + line);
//				// }
//			}

			document.close();

		} catch (Exception e) {

			e.printStackTrace();
		}

	}
}
