package com.shop.ecom.order.domain.user.vo;

import com.shop.ecom.shared.error.domain.Assert;

public record UserLastname(String value) {
  public UserLastname {
    Assert.field("value", value).maxLength(255);
  }
}
