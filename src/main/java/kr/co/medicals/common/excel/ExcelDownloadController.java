package kr.co.medicals.common.excel;

import kr.co.medicals.buyer.BuyerListRequest;
import kr.co.medicals.buyer.BuyerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Locale;

@Slf4j
@Controller
public class ExcelDownloadController {
    private final BuyerService buyerService;
    public ExcelDownloadController(BuyerService buyerService) {
        this.buyerService = buyerService;
    }

    @GetMapping("/excelDownload/buyer/list")
    public ModelAndView paymentExcelDownload(Pageable pageable, BuyerListRequest buyerListRequest) throws Exception {
        log.info("Excel Download loaded.");
        SXSSFWorkbook workbook = (SXSSFWorkbook) buyerService.getBuyerInfoForExcelDownload(pageable, buyerListRequest);
        return new ModelAndView("excelDownloadView")
                .addObject("locale", Locale.KOREA)
                .addObject("workbook", workbook)
                .addObject("workbookName", "testFile");
    }

}
