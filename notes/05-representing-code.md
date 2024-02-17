# Representing Code

## Visitor Pattern

### OOP

- Adding new class: easy
- Adding new method: harder
- [Playground](https://www.typescriptlang.org/play?#code/IYIwzgLgTsDGEAJYBthjAgQmgpgUQA8AHKBAbwCgEFRIZ4ESBLAOwgDVhkBXHALgQAKAJQIAvAD4EdVgHMA3BQC+FCijQZCJAHLcAtiBykmeoshx6cbDNjD5ipStVgB7FnW7wXUQUW4hkJlgEADcuXgEWfUMoUTIVKkYoVg5wnHEhUUkEAANdAyMBABIyCAALJjAAOjCeHCUcxQT1dAQtKABlaDkEEzMLKwgbXHbyRNd3aE8Ib19-QODaiOlullk4hOpmNk46jJFxKRyu5LWEFwAzBHM18oQS8sqatKqb2XKGpoogA)

```ts
abstract class BaseExpr {
  abstract printValue: () => string;
}

class ExprNumber implements BaseExpr {
  constructor(public value: number) {}

  printValue = () => `Number: ${this.value}`;
}

class ExprString implements BaseExpr {
  constructor(public value: string) {}

  printValue = () => `String of length ${this.value.length}`;
}
```

### Functional

- Adding new type: difficult
- Adding new method: easy
- [Playground](https://www.typescriptlang.org/play?#code/JYOwLgpgTgZghgYwgAgKIA8AOUByBXAWwCNpkBvAKGWTDgHMAuZAIhEJKmYG4rkA3OABs8EJm2LQeAXwoVQkWIhQZsAZTBRQdcr1qMWAZw1buvAcNHIjmkHWmywAT0zKsUZAF40b-BPcAfbzVjWx4KBAB7ECN+IREABRsFT2QACgg3JhUoAEomay1PAD4dagMAd2AwBAALNIzsADo9HNLqZAQ4AxRWdmhmBl525CgIMDwoEGQAA18OJgASMgaoRvMRKWmeYc7uwxC6AaH20fHJmfUbbQiYZEEIWzA6pZW1uIhG+8eaze3kGSkPCAA)

```ts
interface ExprNumber {
  tag: "number";
  value: number;
}

interface ExprString {
  tag: "string";
  value: string;
}

type Expr = ExprNumber | ExprString;

const valuePrinter = (expr: Expr): string => {
  switch (expr.tag) {
    case "number":
      return `Number: ${expr.value}`;
    case "string":
      return `String of length ${expr.value.length}`;
  }
};
```

### Visitor

- Tries to emulate the functional way in Object-Oriented Language
- [Playground](https://www.typescriptlang.org/play?#code/IYIwzgLgTsDGEAJYBthjAgogDwA5QQG8AoBBOWAU1wgAoBKIgX2JeJTQx3wDkBXALYhKBStgiUAdgBMueAiTKwA9pMhQ+8ZVFq4+IZAEtYCSYIBcpwcKiNFZBGD64RDANykELNh3RZ5AMrQhpIA5ghiEjJy+ESeKmrQmhDauvpGJgBuwMh8lJbqIaF2nmROLjr0HmTexMQhElAAZnCU-vgAaoZghilQADwASgB8cWSZ3b38Qq5i+JbcUNM29JaD1QgTPRBBUEW0c1ALgcFhqwjrrHUJkJs5eV3b2sedk339hWGjALxjm2-LESWA7yRjfUYAA0BRwQABJCIcAHRmARMCEAGk8W16uyKwMOYMhuLCCGUTQQyCkoQgAAs4Qj5IjsrlKIjKWFaWjMUwPEA)

```ts
abstract class Expr {
  accept() {}
}

class ExprNumber extends Expr {
  constructor(public num: number) {
    super();
  }
}

class ExprString extends Expr {
  constructor(public value: string) {
    super();
  }
}

interface ExprVisitor<R> {
  visitNumber(expr: ExprNumber): R;
  visitString(expr: ExprString): R;
}

const valueVisitor: ExprVisitor<string> = {
  visitNumber: (expr) => `Number: ${expr.num}`,
  visitString: (expr) => `String of length ${expr.value.length}`,
};
```
