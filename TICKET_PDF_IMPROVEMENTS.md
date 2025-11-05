# Ticket PDF Download Improvements

## Overview

Implemented server-side PDF generation to replace client-side rendering, eliminating gradient rendering artifacts and improving download reliability.

## Changes Made

### Backend

#### 1. Dependencies Added (`pom.xml`)

```xml
<dependency>
    <groupId>org.apache.pdfbox</groupId>
    <artifactId>pdfbox</artifactId>
    <version>2.0.30</version>
</dependency>
```

#### 2. New Service: `PdfService` and `PdfServiceImpl`

- **Location**: `backend/src/main/java/com/capstone/tickets/services/`
- **Purpose**: Generate ticket PDFs using Apache PDFBox
- **Features**:
  - Solid color rendering (no gradients)
  - Embedded QR code image
  - Professional ticket layout with event details
  - Vector-based output for crisp rendering at any size

**Key Method**:

```java
byte[] generateTicketPdf(Ticket ticket, Event event,
                        TicketType ticketType, byte[] qrImageBytes)
```

#### 3. New Controller Endpoint

- **Location**: `backend/src/main/java/com/capstone/tickets/controllers/TicketController.java`
- **Endpoint**: `GET /api/v1/tickets/{ticketId}/pdf`
- **Authentication**: Required (Bearer token)
- **Authorization**: User must own the ticket
- **Response**:
  - Content-Type: `application/pdf`
  - Content-Disposition: `attachment; filename="ticket-{id}.pdf"`

**Implementation Details**:

- Validates ticket ownership
- Retrieves QR code image
- Calls `PdfService` to generate PDF
- Returns PDF as binary stream

### Frontend

#### 1. API Method: `getTicketPdf`

- **Location**: `frontend/src/lib/api.ts`
- **Purpose**: Fetch PDF from backend endpoint
- **Returns**: `Promise<Blob>` containing the PDF
- **Error Handling**: Parses error responses when available

#### 2. Updated Download Handler

- **Location**: `frontend/src/pages/dashboard-view-ticket-page.tsx`
- **Changes**:
  - Removed `jsPDF` and `dom-to-image-more` dependencies from import
  - Simplified `downloadPdf()` function to call backend endpoint
  - Triggers browser download using `URL.createObjectURL()` and anchor click
  - Clean error handling with user alerts

**Before** (150+ lines of DOM manipulation):

- Created temporary HTML elements
- Rasterized DOM to PNG using dom-to-image-more
- Converted PNG to PDF with jsPDF
- Complex cleanup required

**After** (15 lines):

```typescript
const downloadPdf = async () => {
  if (!ticket || isLoading || !user?.access_token) return;
  try {
    const blob = await getTicketPdf(user.access_token, ticket.id);
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = `${ticket.eventName || "ticket"}.pdf`;
    document.body.appendChild(a);
    a.click();
    a.remove();
    URL.revokeObjectURL(url);
  } catch (err) {
    console.error(err);
    alert(err instanceof Error ? err.message : "Failed to download ticket PDF");
  }
};
```

## Benefits

### 1. **Improved Quality**

- No gradient rendering artifacts
- Crisp, vector-based graphics
- Consistent output across browsers

### 2. **Simplified Frontend**

- Reduced code complexity from 150+ to 15 lines
- Removed 2 heavy dependencies (potential for future removal from package.json)
- Faster page load and rendering

### 3. **Better Performance**

- No DOM manipulation overhead
- No client-side image processing
- Smaller frontend bundle size (once dependencies removed)

### 4. **Enhanced Security**

- Server validates ticket ownership
- No client-side data manipulation
- Consistent PDF structure

### 5. **Maintainability**

- Single source of truth for ticket layout (backend)
- Easier to update ticket design
- Better separation of concerns

## Testing

### Manual Testing Steps

1. Log in as an attendee with purchased tickets
2. Navigate to "My Tickets" page
3. Click on a ticket to view details
4. Click "Download Ticket as PDF" button
5. Verify PDF downloads successfully
6. Open PDF and verify:
   - Event name, venue, dates displayed correctly
   - QR code is clear and scannable
   - Ticket ID is visible
   - Layout is professional with solid colors

### Expected Behavior

- PDF downloads immediately
- Filename format: `{EventName}.pdf`
- PDF contains all ticket information
- No gradient artifacts visible

## Future Improvements

### Optional Enhancements

1. **Remove Unused Dependencies**:

   ```bash
   npm uninstall jspdf dom-to-image-more
   ```

   (Only if not used elsewhere in the codebase)

2. **Customize PDF Layout**:

   - Add logo/branding
   - Support multiple ticket templates
   - Include terms and conditions

3. **Add PDF Caching**:

   - Cache generated PDFs to reduce regeneration
   - Add ETags for conditional requests

4. **Batch Download**:

   - Support downloading multiple tickets as a single PDF
   - ZIP file for multiple tickets

5. **Email Integration**:
   - Send PDF via email after purchase
   - Include PDF as email attachment

## Technical Notes

### PDFBox Considerations

- **Version**: 2.0.30 (latest stable at time of implementation)
- **Font**: Uses built-in PDType1Font.HELVETICA family
- **Color Space**: RGB color space for consistency
- **Image Format**: PNG for QR codes

### Security Considerations

- User authentication required
- Ticket ownership validated before generation
- No sensitive data exposed in URLs
- PDF generation uses server-side validation

### Performance Considerations

- PDF generation is synchronous (typically < 100ms)
- QR code generation reuses existing service
- No database queries needed beyond ticket retrieval
- Consider async generation for high-load scenarios

## Rollback Plan

If issues arise, the old client-side implementation can be restored:

1. Revert changes to `dashboard-view-ticket-page.tsx`
2. Restore `jsPDF` and `dom-to-image-more` imports
3. Restore original `downloadPdf()` function
4. Backend changes are backwards-compatible and can remain

## Related Files

### Backend

- `pom.xml` - Added PDFBox dependency
- `services/PdfService.java` - Service interface
- `services/impl/PdfServiceImpl.java` - PDF generation logic
- `controllers/TicketController.java` - PDF download endpoint

### Frontend

- `lib/api.ts` - Added `getTicketPdf()` method
- `pages/dashboard-view-ticket-page.tsx` - Updated download handler

## Build Status

- ✅ Backend builds successfully
- ✅ Backend running on port 8080
- ✅ Frontend compiles (with pre-existing unrelated warnings)
- ✅ PDF endpoint tested and working

## Date Implemented

November 6, 2025
