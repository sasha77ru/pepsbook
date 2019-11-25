package ru.sasha77.spring.pepsbook.models;


import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.sasha77.spring.pepsbook.webModels.UserSimple;

import javax.persistence.*;
import java.util.*;

@ToString(onlyExplicitlyIncluded = true)
@Data
@NoArgsConstructor(access=AccessLevel.PRIVATE, force=true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name="users")
public class User implements UserDetails {
    @Setter
	@EqualsAndHashCode.Include
	@Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;

	@Setter
	@ToString.Include
	private String name;

	@SuppressWarnings("unused")
	@Setter
	@ToString.Include
	@Column(unique = true)
	private String email;

	@SuppressWarnings("unused")
	@Setter
	@ToString.Include
	private String country;

	@Setter
	@Column(unique = true)
	private String username;

	@Setter
	private String password;

	@Setter
	private boolean enabled;

	@Setter
	@ManyToMany(/*cascade = {CascadeType.ALL},*/ fetch = FetchType.EAGER)
	@JoinTable(name="friendship",
			joinColumns = @JoinColumn(name="user_id"),
			inverseJoinColumns = @JoinColumn(name="friend_id"))
	private Set<User> friends = new HashSet<>();

	@Setter
	@ManyToMany(mappedBy = "friends", fetch = FetchType.EAGER)
	private Set<User> mates = new HashSet<>();

	public User(String name, String email, String country, String username, String password) {
		this.name = name;
		this.email = email;
		this.country = country;
		this.username = username;
		this.password = password;
		this.enabled = true;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}
}

