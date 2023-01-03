package kr.co.medicals.buyer.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.co.medicals.buyer.domain.entity.ShippingAddress;
import kr.co.medicals.common.util.SessionUtil;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShippingAddressDto {

    private Long seq;
    private String buyerCode;
    private int addressSeq;
    private String address;
    private String addressPostNo;
    private String addressDetail;
    private String addressName;
    private String addressTel;
    private String delYn;
    private String createdId;
    private LocalDateTime createdDatetime;
    private String modifiedId;
    private LocalDateTime modifiedDatetime;

    public ShippingAddressDto(){}

    @Builder(builderClassName = "byCreate", builderMethodName = "byCreate")
    public ShippingAddressDto(Long seq, String buyerCode, int addressSeq, String address, String addressPostNo, String addressDetail, String addressName, String addressTel, String delYn, String createdId, LocalDateTime createdDatetime, String modifiedId, LocalDateTime modifiedDatetime) {
        this.seq = seq;
        this.buyerCode = buyerCode;
        this.addressSeq = addressSeq;
        this.address = address;
        this.addressPostNo = addressPostNo;
        this.addressDetail = addressDetail;
        this.addressName = addressName;
        this.addressTel = addressTel;
        this.delYn = delYn;
        this.createdId = createdId;
        this.createdDatetime = createdDatetime;
        this.modifiedId = modifiedId;
        this.modifiedDatetime = modifiedDatetime;
    }

    @Builder(builderClassName = "selectByEntity", builderMethodName = "selectByEntity")
    public ShippingAddressDto(ShippingAddress shippingAddress){
        this.seq = shippingAddress.getSeq();
        this.buyerCode = shippingAddress.getBuyerCode();
        this.addressSeq = shippingAddress.getAddressSeq();
        this.address = shippingAddress.getAddress();
        this.addressPostNo = shippingAddress.getAddressPostNo();
        this.addressDetail = shippingAddress.getAddressDetail();
        this.addressName = shippingAddress.getAddressName();
        this.addressTel = shippingAddress.getAddressTel();
        this.delYn = shippingAddress.getDelYn();
        this.createdId = shippingAddress.getCreatedId();
        this.createdDatetime = shippingAddress.getCreatedDatetime();
        this.modifiedId = shippingAddress.getModifiedId();
        this.modifiedDatetime = shippingAddress.getModifiedDatetime();
    }

    public ShippingAddress insertEntity(String buyerCode, int count){
        return ShippingAddress
                .builder()
                .buyerCode(buyerCode)
                .addressSeq(count + 1)
                .address(this.address)
                .addressPostNo(this.addressPostNo)
                .addressDetail(this.addressDetail)
                .addressName(this.addressName)
                .addressTel(this.addressTel)
                .delYn("N")
                .createdId(SessionUtil.getUserCode())
                .createdDatetime(LocalDateTime.now())
                .build();
    }

    public ShippingAddress updateEntity(ShippingAddressDto shippingAddressDto){
        return ShippingAddress
                .builder()
                .seq(shippingAddressDto.getSeq())
                .buyerCode(shippingAddressDto.getBuyerCode())
                .addressSeq(shippingAddressDto.getAddressSeq())
                .address(this.address)
                .addressPostNo(this.addressPostNo)
                .addressDetail(this.addressDetail)
                .addressName(this.addressName)
                .addressTel(this.addressTel)
                .delYn(shippingAddressDto.getDelYn())
                .createdId(shippingAddressDto.getCreatedId())
                .createdDatetime(shippingAddressDto.getCreatedDatetime())
                .modifiedId(SessionUtil.getUserCode())
                .modifiedDatetime(LocalDateTime.now())
                .build();
    }

    public ShippingAddress updateDelYEntity(){
        return ShippingAddress
                .builder()
                .seq(this.seq)
                .buyerCode(this.buyerCode)
                .addressSeq(this.addressSeq)
                .address(this.address)
                .addressPostNo(this.addressPostNo)
                .addressDetail(this.addressDetail)
                .addressName(this.addressName)
                .addressTel(this.addressTel)
                .delYn("Y")
                .createdId(this.createdId)
                .createdDatetime(this.createdDatetime)
                .modifiedId(SessionUtil.getUserCode())
                .modifiedDatetime(LocalDateTime.now())
                .build();
    }

}
