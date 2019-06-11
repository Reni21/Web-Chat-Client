package entity;

import lombok.*;

@EqualsAndHashCode
@Getter
@Setter
@AllArgsConstructor
@ToString
public class Credentials {
    private String login;
    private String pass;

}
