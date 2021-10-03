package pdf.read;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
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

			File file = new File("pdfs/GLRP04R_PFULLER_GLBT01C1_062831_1.pdf");

			PDDocument document = PDDocument.load(file);

			fixNumerics(document);

			alignHeaders(document);

			document.save("pdfs/GENERATED_GLBT01C1.pdf");

			document.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void fixNumerics(PDDocument doc) throws IOException {

		PDPageTree pages = doc.getDocumentCatalog().getPages();
		for (PDPage page : pages) {
			PDFStreamParser parser = new PDFStreamParser(page);
			parser.parse();
			List<Object> tokens = parser.getTokens();
			for (int j = 0; j < tokens.size(); j++) {
				Object next = tokens.get(j);
				if (next instanceof Operator) {
					Operator op = (Operator) next;
					// Tj and TJ are the two operators that display strings in a PDF
					// Tj takes one operator and that is the string to display so lets update that
					// operator
					if (op.getName().equals("Tj")) {
						COSString previous = (COSString) tokens.get(j - 1);
						String string = previous.getString();
						string = string.replace("(", "");
						string = string.replace(")", "");
						if (string.endsWith("-")) {
							try {
								String tempPossibleNumber = string.replace("-", "").replace(",", "");
								Double d = Double.valueOf(tempPossibleNumber);
								d *= -1;
								DecimalFormat df = new DecimalFormat("#,###.00");
								String newString = df.format(d);
								if (string.length() != newString.length()) {
									StringBuffer buf = new StringBuffer();
									for (int i = 0; i < string.length() - newString.length() - 1; i++) {
										buf.append(" ");
									}
									buf.append(newString);
									string = buf.toString();
								} else {
									string = newString;
								}

							} catch (Exception e) {
							}
						}

						previous.setValue(string.getBytes());
					} else if (op.getName().equals("TJ")) {
						COSArray previous = (COSArray) tokens.get(j - 1);
						for (int k = 0; k < previous.size(); k++) {
							Object arrElement = previous.getObject(k);
							if (arrElement instanceof COSString) {
								COSString cosString = (COSString) arrElement;
								String string = cosString.getString();
								string = string.replace("(", "");
								string = string.replace(")", "");
								if (string.endsWith("-")) {
									try {
										String tempPossibleNumber = string.replace("-", "").replace(",", "");
										Double d = Double.valueOf(tempPossibleNumber);
										d *= -1;
										string = d.toString();
									} catch (Exception e) {
									}
								}
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

	}

	public void alignHeaders(PDDocument document) {

		try {

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

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
