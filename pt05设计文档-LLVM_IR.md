### llvm指令类型

[instruction reference](https://llvm.org/docs/LangRef.html#instruction-reference)

```shell
add, sub, mult, sdiv(for signed integer)
and, or	# for i1
alloca, load, store
icmp

br		# 一个参数/三个参数
call, ret

```

* [ret](https://llvm.org/docs/LangRef.html#i-ret),[call](https://llvm.org/docs/LangRef.html#call-instruction)
* [br](https://llvm.org/docs/LangRef.html#br-instruction)
* [icmp](https://llvm.org/docs/LangRef.html#icmp-instruction)

#### 1. main()函数

```c
int main() {
    return 123;
}
```

```
define dso_local i32 @main(){
    ret i32 123
}
```

#### 2.常量表达式

遍历树前序输出就ok

#### 3.局部变量

##### 赋值

```c
int a = 123 - 122;
return a;
```

```shell
 %1 = alloca i32
 # 计算表达式，存储局部变量
 %2 = sub i32 123, 122
 store i32 %2, i32* %1
 # return语句，取用局部变量
 %3 = load i32, i32* %1
 ret i32 %3
```

##### getint函数

```c
a = getint();
```

```shell
%1 = alloca i32
%2 = call i32 @getint()
store i32 %2, i32* %1
```

对于有参数的函数，需要设置传参列表

#### 4.条件表达式

使用`icmp`语句，注意这样得到的是`i1`类型的数值。

##### if块

```c
if (cond) {
    ...
} else {
    ...    
}
```

```shell
    %1 = icmp neq, %x, 0	# 或者别的什么比较指令
    br i1 %1, label %2, label %3
2:
	...
	br label %4
3:
	...
	br label %4
4:
```

##### 短路求值

```c
if (a && b) {
   ...
}
```

```c
	%a = ...
    br i1 %a, label %1, label %2
1:
    %b = ...
	br i1 %b, label %3, label %2
3:
	
2:

```

##### 逻辑运算

```shell
# !a
%2 = load i32, i32* %1
%3 = icmp eq i32 %2, 0
%4 = zext i1 %3 to i32
```

#### 5.全局变量，作用域

##### 全局变量

* 用@开头前缀表示
* 然，怎么给它赋值？

```shell
store i32 2, i32 @a 
```

##### 作用域

通过符号表机制实现，为内部和外部变量分布分配两块内存区域。

```c
int a = 4;
{
    int a;
    a = 3;
    printf("%d", a)
}
printf("%d", a);
```

#### 6. 循环

​	while, break, continue

* 对于`br`语句，规定第一个标签永远在`br`的正下方。因为`mips`最终实现跳转时只有一个

```c
while (a) {
    ...
    continue;
    ...
    break;
    ...
}
```

```assembly
condbegin:
	%1 = icmp bne %x, 0
    br %1, loopbegin, loopend
loopbegin:
	...
	br condbegin
	...
	br loopend
	...
loopend:	
```

#### 7.数组

#### 8.函数调用