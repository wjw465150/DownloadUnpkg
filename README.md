# 把UNPKG网站中指定目录的文件全部下载到本地

例如: 现在vue.js使用很广泛，饿了么的element-ui基于vue.js开发的ui框架。官方提供了unpkg的访问地址[https://unpkg.com/element-ui@2.4.6/](https://unpkg.com/element-ui@2.4.6/)。 可以直接在项目中使用cdn引入!

但是有时候需要下载到本地项目中引入(比如不能上网的时候)，我们可以用到的时候，事先在网上一个一个的下载，很麻烦的。简单写一个`Groovy`的小脚本，直接从unpkg上下载到本地磁盘。代码亲测ok。自己一直在用!
