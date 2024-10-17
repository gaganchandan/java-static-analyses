class A {
  A next;

  A() {
    next = null;
  }
}

class Example1 {
  static void main(String[] args) {
    A c, head, y;
    head = new A();
    c = head;
    while (c != head) {
      y = new A();
      c.next = y;
      c = c.next;
    }
    c = head;
    while (c != head) {
      c = c.next;
    }
  }
}
