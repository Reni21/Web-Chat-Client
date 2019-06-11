import lombok.*;

@EqualsAndHashCode
@Getter
@Setter
@AllArgsConstructor
@ToString
public class CodeMessage {
    private final int responseCode;
    private final String responseMessage;
}
