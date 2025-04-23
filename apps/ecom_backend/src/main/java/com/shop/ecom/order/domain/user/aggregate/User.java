package com.shop.ecom.order.domain.user.aggregate;

import com.shop.ecom.order.domain.user.vo.*;
import com.shop.ecom.shared.error.domain.Assert;
import org.jilt.Builder;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Builder
public class User {

  private UserFirstname firstname;
  private UserLastname lastname;
  private UserEmail email;
  private UserPublicId userPublicId;
  private UserImageUrl imageUrl;
  private UserAddress userAddress;
  private Instant lastModifiedDate;
  private Instant createdDate;
  private Instant lastSeen;
  private Set<Authority> authorities;
  private Long dbId;

  public User(UserFirstname firstname,UserLastname lastname,UserEmail email,UserPublicId userPublicId,UserImageUrl imageUrl,UserAddress userAddress,Instant lastModifiedDate,Instant createdDate,Instant lastSeen,Set<Authority> authorities,Long dbId  ) {
    this.firstname = firstname;
    this.lastname = lastname;
    this.email = email;
    this.userPublicId = userPublicId;
    this.imageUrl = imageUrl;
    this.userAddress = userAddress;
    this.lastModifiedDate = lastModifiedDate;
    this.createdDate = createdDate;
    this.lastSeen = lastSeen;
    this.authorities = authorities;
    this.dbId = dbId;
  }

  private void assertMandatoryFields() {
    Assert.notNull("firstname", firstname);
    Assert.notNull("lastname", lastname);
    Assert.notNull("email", email);
    Assert.notNull("authorities", authorities);
  }

  public void updateFromUser(User user) {
    this.email = user.email;
    this.imageUrl = user.imageUrl;
    this.firstname = user.firstname;
    this.lastname = user.lastname;
  }

  public void initFieldForSignup(){
    this.userPublicId = new UserPublicId(UUID.randomUUID());
  }

  public static User fromTokenAttributes(Map<String, Object> attributes, List<String> rolesFromAccessToken) {
    UserBuilder userBuilder = UserBuilder.user();

    if (attributes.containsKey("preferred_email")) {
      userBuilder.email(new UserEmail(attributes.get("preferred_email").toString()));
    }
    if (attributes.containsKey("last_name")) {
      userBuilder.lastname(new UserLastname(attributes.get("last_name").toString()));
    }
    if (attributes.containsKey("first_name")) {
      userBuilder.firstname(new UserFirstname(attributes.get("first_name").toString()));
    }
    if (attributes.containsKey("picture")) {
      userBuilder.imageUrl(new UserImageUrl(attributes.get("picture").toString()));
    }
    if (attributes.containsKey("last_signed_in")) {
      userBuilder.lastSeen(Instant.parse(attributes.get("last_signed_in").toString()));
    }

    Set<Authority> authorities = rolesFromAccessToken.stream()
      .map(authority -> AuthorityBuilder.authority().name(new AuthorityName(authority)).build())
      .collect(Collectors.toSet());

    userBuilder.authorities(authorities);

    return userBuilder.build();
  }

  public UserLastname getLastname() {
    return lastname;
  }

  public UserFirstname getFirstname() {
    return firstname;
  }

  public UserEmail getEmail() {
    return email;
  }

  public UserPublicId getUserPublicId() {
    return userPublicId;
  }

  public UserImageUrl getImageUrl() {
    return imageUrl;
  }

  public Instant getLastModifiedDate() {
    return lastModifiedDate;
  }

  public Instant getCreatedDate() {
    return createdDate;
  }

  public Set<Authority> getAuthorities() {
    return authorities;
  }

  public Long getDbId() {
    return dbId;
  }

  public UserAddress getUserAddress() {
    return userAddress;
  }

  public Instant getLastSeen() {
    return lastSeen;
  }
}
