public class Member {
  private long id;
  private String name;
  private String email;
  private String availability;

  public Member(long id, String name, String email, String availability) {
    this.id = id;
    this.name = name;
    this.email = email;
    this.availability = availability;
  }

  public long getId() { return id; }
  public String getName() { return name; }
  public String getEmail() { return email; }
  public String getAvailability() { return availability; }
}
