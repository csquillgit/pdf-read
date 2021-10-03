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
import org.apache.pdfbox.pdmodel.common.PDStream;

public class PdfReadMain {

	public static void main(String args[]) {
		new PdfReadMain().process();
	}

	private void process() {

		try {

			File file = new File("pdfs/GLRP04R_PFULLER_GLBT01C1_062831_2.pdf");

			PDDocument document = PDDocument.load(file);

			searchReplace("PFULLER", "CHUCK", "UTF-8", false, document);

			// addToDoc(document);

			bufferHeaders(document);

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

			PDStream updatedStream = new PDStream(doc);
			OutputStream out = updatedStream.createOutputStream();
			ContentStreamWriter tokenWriter = new ContentStreamWriter(out);
			tokenWriter.writeTokens(tokens);

			out.close();

			page.setContents(updatedStream);
		}

		doc.save("pdfs/chuck1.pdf");
		// doc.close();
	}

	public void bufferHeaders(PDDocument x) {

		try {

			File file = new File("pdfs/GLRP04R_PFULLER_GLBT01C1_062831_2.pdf");

			PDDocument document = PDDocument.load(file);

			for (int i = 0; i < document.getDocumentCatalog().getPages().getCount(); i++) {

				PDPage page = (PDPage) document.getDocumentCatalog().getPages().get(i);

				float[] headerCoordinates = PDFTextLocator.getCoordiantes(document, "<------", i);

				// if header not found continue next page
				if (headerCoordinates == null || headerCoordinates.length == 0 || headerCoordinates[0] == -1) {
					continue;
				}

				PDPageContentStream cos = new PDPageContentStream(document, page,
						PDPageContentStream.AppendMode.PREPEND, true);

				// draw a line up to header
				cos.moveTo(0, headerCoordinates[1]);
				cos.lineTo(headerCoordinates[0], headerCoordinates[1]);
				cos.setLineWidth(.5f);
				cos.setLineCapStyle(1);
				cos.stroke();

				cos.close();
			}

			document.save("pdfs/chuck2.pdf");

			document.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
