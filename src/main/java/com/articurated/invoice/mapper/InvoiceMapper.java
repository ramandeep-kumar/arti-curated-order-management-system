package com.articurated.invoice.mapper;

import com.articurated.invoice.domain.Invoice;
import com.articurated.invoice.dto.InvoiceResponse;

public interface InvoiceMapper {
    InvoiceResponse toResponse(Invoice invoice);
}
