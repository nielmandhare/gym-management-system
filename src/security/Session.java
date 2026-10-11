package security;

public final class Session {
    public record Identity(int userId, String username, String role, Integer trainerId, Integer memberId) { }
    private static Identity current;
    private Session() { }
    public static Identity current() { return current; }
    static void start(Identity identity) { current=identity; }
    public static void logout() { current=null; }
    public static void requireAdmin() {
        if(current==null || !"ADMIN".equals(current.role())) throw new SecurityException("Administrator access required.");
        Accounts.requireActive();
    }
    public static void requireTrainer() {
        if(current==null || !"TRAINER".equals(current.role())) throw new SecurityException("Trainer access required.");
        Accounts.requireActive();
    }
    public static void requireMember() {
        if(current==null || !"MEMBER".equals(current.role())) throw new SecurityException("Member access required.");
        Accounts.requireActive();
    }
}
