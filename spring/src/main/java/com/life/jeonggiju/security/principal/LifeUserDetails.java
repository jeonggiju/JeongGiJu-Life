package com.life.jeonggiju.security.principal;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.life.jeonggiju.security.dto.UserDto;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class LifeUserDetails implements UserDetails {

	private final UserDto userDto;

	@Override
	public boolean isAccountNonExpired() {
		return UserDetails.super.isAccountNonExpired();
	}

	@Override
	public boolean isAccountNonLocked() {
		return UserDetails.super.isAccountNonLocked();
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return UserDetails.super.isCredentialsNonExpired();
	}

	@Override
	public boolean isEnabled() {
		return UserDetails.super.isEnabled();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority(userDto.getAuthority().name());
		return List.of(simpleGrantedAuthority);
	}

	@Override
	public @Nullable String getPassword() {
		return userDto.getPassword();
	}

	@Override
	public String getUsername() {
		return userDto.getEmail();
	}

	public String getEmail(){
		return userDto.getEmail();
	}

	public UUID getId(){
		return userDto.getId();
	}
}
