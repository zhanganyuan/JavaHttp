# JavaHttp
http examples for GET and POST method， especially for POST a formdata with a file.

还没有经过测试，不过应该问题不大，，，

## GET 和 POST 有什么区别。

GET 和 POST 是 HTTP 最常见的请求类型之二。他们两个其实只有一个区别，那就是 GET 请求的参数只需要 URL，不需要传递正文。而 POST 不仅需要 URL 而且还需要传递正文。

而在这里有必要知道一点，什么是 URL。 比如：http://example.com/index.jsp?key1=value1&key2=value2。 这实际上才被认为是一个 URL。也就是说参数是被包含在 URL 里面的。所以才说 GET 只需要 URL 就可以发送请求，如果 GET 请求需要传递参数的话就把参数一并放在网址的后面。但是有些需要传递的数据并不都是简单的字符串，比如我们要上传一个文件，这时候，文件的内容也是要传给服务器的，那这时候我们就只能把数据放在正文里面。这时候就需要用到 POST 方法而不能是 GET 方法。

整体上来说 GET 一般用来请求一些静态资源，而 POST 方法主要用作动态的请求。

> PS：POST 请求的正文都是以 byte 数组的形式。

## Java 中如何使用 Http

这就不得不要去了解一个类，叫`URLConnection`。

基本用法如下：

`URL url= new URL("http://example.com?key=value");` //创建一个 URL 对象 

`HttpURLConnection conn= (HttpURLConnection) url.openConnection();`//用创建的 URL 对象创建 URLConnection, 注意 URLConnection 才是可以实例化的类,HTTPURLConnection 是一个抽象类, 不能直接实例化。conn 实际上是一个指向 URLConnection 对象的引用。

`conn.connect();`//连接 

`BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));`//读取返回数据的 reader

`String line;`

//读取返回 

`while ((line = in.readLine()) != null) {`

    `res += line;`
    
`}`

上面的是基本的 GET 操作。

## POST 与 GET 有几点不同。

 1. POST 在 connect（）之前要做如下几个设置。
 
 `httpUrlConnection.setDoOutput(true);`//设置是否输出，默认 false(GET)。POST 因为要发送正文，所以设置为 true。
 
`httpUrlConnection.setDoInput(true);` //设置是否输入，默认 true。

`httpUrlConnection.setUseCaches(false);` //设置是否使用缓存，POST 一般不使用缓存，而 GET 一般使用（因为 GET 一般请求的是静态资源如 css，所以使用缓存能够提高效率。而 POST 请求的数据一般不具有持久性，所以不做缓存。）

2. 请求的头部（Header）里面，设置正文的类型（Content-Type）

在上面的 GET 示例中，我没有写 Header 的设置，这是因为大部分的 Header 都不太重要，设置与否并不是很重要。但是 POST，有一个 Header 就必须要设置了，那就是 Content-Type。

`httpUrlConnection.setRequestProperty("Content-type", "application/x-java-serialized-object"); `

Content-Type 具体值和正文的内容有关。

至于其他的请求头部。看下面一张截图。

3. 指定请求的方法为 POST，默认是 GET

`httpUrlConnection.setRequestMethod("POST"); `

4. 发送正文 

`OutputStream out = httpUrlConnection.getOutputStream();`

`out.write(data);`

## 理一下思路 

GET 方法，只需要我们设置 URL（里面包含参数），就可以获取请求。先生成一个 URL 对象。然后用 URL 对象产生 URLConnection 对象，然后设置 Header 并调用 connect() 方法。最后使用 URLConnection 对象产生 IutputStream, 然后输出返回结果就行了。

POST 方法，在设置 URL 之后，还需要设置相关的参数（是否使用 cache、请求方法等），还要设置 Header 的 Content-Type。最后还要输入正文。所以 POST 的顺序就是先生成一个 URL 对象。然后用 URL 对象产生一个 URLConnection 对象，并设置 Header（尤其是 Content-Type）并调用 connect() 方法。最后使用 URLConnection 对象产生 OnputStream，发送正文，然后用 InputStream 读取返回结果。

## 正文的格式 

先给一个示例的正文格式吧（【】里的表示说明文字，[] 里是可选的内容,{} 里表示该填入的值），如下：

–【必须带的两个中横线】——–sj38dkflb【随机] 字符串，作为边界】

Content-Disposition: form-data; name=”{}”【name 是作为 key 值，而 filename 是当此项是文件的时候，传的文件名，下面的 value 则是 value 值（不管是文件与否，formdata 都是表单，表单就是 key-value 键值对，只不过文件的 value 是文件内容，另外还要加上文件名而已）】[; filename=”{}【当为文件的时候，这个要加上】]

[Content-Type: {}【这里和 Header 里的 Content-Type 不是同一个东西，比如，如果是 jpg 的话，这里就应该是 image/jpg, 如果不是文件，此处不需要 Content-Type】]

/r/n【空出一行】

{}【此处要填入正文内容，如果是文件，那么就应该是文件的内容，如果不是文件而是普通表单，那么就填入表单的 value 值】

–【必须】——–sj38dkflb【边界】–【必须】

上面就是正文的内容了，我们要做的就是拼接正文的内容，然后把正文通过前面说的 OutputStream 发送出去。
