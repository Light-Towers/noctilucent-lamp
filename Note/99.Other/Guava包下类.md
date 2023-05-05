不可变的集合、线程安全、元素不可重复。 会获取传入对象的一个副本，而不会改变原来的对象

```java
Integer a = 3;
ImmutableSet<Integer> set5 = ImmutableSet .<Integer>of(a, 2);
// 输出[3, 2]
System.out.println(set5);
a = 5;
// 输出[3, 2]
System.out.println(set5);
```

ImmutableSet和ImmutableList一样，也有两种构建Immutable对象的方法，一种是静态的of方法，一种是静态内部类Builder。

* 静态内部类Builder模式

静态内部类的源码：

```java
  public static class Builder<E>
    extends ImmutableCollection.Builder<E>
  {
    Object[] contents;

    int size;

    public Builder()
    {
      this(4);
    }

    Builder(int capacity) {
      Preconditions.checkArgument(capacity >= 0, "capacity must be >= 0 but was %s", new Object[] { Integer.valueOf(capacity) });
      this.contents = new Object[capacity];
      this.size = 0;
    }

    Builder<E> ensureCapacity(int minCapacity)
    {
      if (this.contents.length < minCapacity) {
        this.contents = ObjectArrays.arraysCopyOf(this.contents, expandedCapacity(this.contents.length, minCapacity));
      }

      return this;
    }


    public Builder<E> add(E element)
    {
      ensureCapacity(this.size + 1);
      this.contents[(this.size++)] = Preconditions.checkNotNull(element);
      return this;
    }
    public Builder<E> add(E... elements)
    {
      for (int i = 0; i < elements.length; i++) {
        ObjectArrays.checkElementNotNull(elements[i], i);
      }
      ensureCapacity(this.size + elements.length);
      System.arraycopy(elements, 0, this.contents, this.size, elements.length);
      this.size += elements.length;
      return this;
    }
    public Builder<E> addAll(Iterable<? extends E> elements)
    {
      if ((elements instanceof Collection)) {
        Collection<?> collection = (Collection)elements;
        ensureCapacity(this.size + collection.size());
      }
      super.addAll(elements);
      return this;
    }

    public Builder<E> addAll(Iterator<? extends E> elements)
    {
      super.addAll(elements);
      return this;
    }
    public ImmutableSet<E> build()
    {
      ImmutableSet<E> result = ImmutableSet.construct(this.size, this.contents);


      this.size = result.size();
      return result;
    }
  }
```

静态内部类模式构建Immutable对象

```java
//使用静态内部类的方式构建不可重复的集合对象
ImmutableSet<Integer> set2 = ImmutableSet .<Integer>builder()
                                             .add(1)
                                             .add(2)
                                             .build();
```
* 使用of静态方法模式构建Immutable对象

```java
//获取空的不可变对象
ImmutableSet<Integer> set = ImmutableSet .<Integer>of();
//获取空的不可变对象，去重
ImmutableSet<Integer> set1 = ImmutableSet .<Integer>of(1, 1);
```

复制其它Collection集合对象的副本到一个Immutable对象中，并且去重

```java
List<Integer> set3 = new ArrayList<Integer>();
set3.add(1);
set3.add(2);
set3.add(1);
set3.add(2);
//复制集合中的对象，并去重
ImmutableSet<Integer> set4 = ImmutableSet.<Integer>copyOf(set3);
```

