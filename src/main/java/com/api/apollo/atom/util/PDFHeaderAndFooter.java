package com.api.apollo.atom.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import lombok.Setter;

@Setter
public class PDFHeaderAndFooter extends PdfPageEventHelper {

    Font footerFont = new Font(Font.FontFamily.UNDEFINED, 12, Font.BOLD);
    private boolean islastPage = false;
    private PdfTemplate t;
    private Image total;

    public void onOpenDocument(PdfWriter writer, Document document) {
        t = writer.getDirectContent().createTemplate(30, 16);
        try {
            total = Image.getInstance(t);
            total.setRole(PdfName.ARTIFACT);
        } catch (DocumentException de) {
            throw new ExceptionConverter(de);
        }
    }

    public void onEndPage(PdfWriter writer, Document document) {
        try {
            if(islastPage) {
                ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, new Phrase("                 Loading Supervisor    " +
                    "      FGS         QC         Transporter         Security         Driver ", footerFont), 220, 40, 0);
            }
            addFooter(writer);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
    private void addFooter(PdfWriter writer){
        PdfPTable footer = new PdfPTable(3);
        try {
            // set defaults
            footer.setWidths(new int[]{35, 4, 1});
            footer.setTotalWidth(527);


            footer.setLockedWidth(true);
            footer.getDefaultCell().setFixedHeight(40);
            footer.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            // add copyright
            footer.addCell(new Phrase("", new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD)));

            // add current page count
            footer.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
            PdfPCell cell = new PdfPCell(new Phrase(String.format("Page %d of", writer.getPageNumber()), new Font(Font.FontFamily.TIMES_ROMAN, 12)));
            cell.disableBorderSide(Rectangle.BOX);
            cell.setPaddingBottom(30);
            cell.setPaddingLeft(5);
            footer.addCell(cell);

            // add placeholder for total page count
            PdfPCell totalPageCount = new PdfPCell(total);
            totalPageCount.setBorder(Rectangle.NO_BORDER);
            totalPageCount.setBorderColor(BaseColor.LIGHT_GRAY);
            footer.addCell(totalPageCount);

            // write page
            PdfContentByte canvas = writer.getDirectContent();
            canvas.beginMarkedContentSequence(PdfName.ARTIFACT);
            footer.writeSelectedRows(0, -1, 40, 50, canvas);
            canvas.endMarkedContentSequence();
        } catch(DocumentException de) {
            throw new ExceptionConverter(de);
        }
    }
    public void onCloseDocument(PdfWriter writer, Document document) {
        int totalLength = String.valueOf(writer.getPageNumber()).length();
        int totalWidth = totalLength * 10;
        ColumnText.showTextAligned(t, Element.ALIGN_RIGHT,
                new Phrase(String.valueOf(writer.getPageNumber()), new Font(Font.FontFamily.TIMES_ROMAN, 12)),
                totalWidth, 2, 0);
    }

}
