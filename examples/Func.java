class A {
  A next;

  A() {
    next = null;
  }
}

class Func {
  public static void main(String... args) {
    A x = new A();
    A y = new A();
    compare(x, y);
  }

  static void compare(A x, A y) {
    if (x.next == y.next) {
      System.out.println("Equal");
    } else {
      System.out.println("Not Equal");
    }
  }
}
