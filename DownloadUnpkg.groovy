import groovy.transform.Field
import groovy.json.JsonSlurper
import org.ccil.cowan.tagsoup.Parser
import groovy.xml.XmlSlurper

@Field
    //要下载的库名字
    String libName = "element-ui"
//String libName = "mint-ui"

@Field
    //要下载的版本号,可以根据实际情况来修改
    String libVersion = "2.13.2"
//String libVersion = "2.2.13"

//下载到本地的根目录,可以根据实际情况来修改
@Field
    String downloadPath = "c:/addons"

@Field
    File fileDownloadPath = new File(downloadPath + "/" + libName + "/" + libVersion)
//先清空目录
fileDownloadPath.deleteDir()
fileDownloadPath.mkdirs()

//UNPKG网站的element-ui项目的根URL
@Field
    String rootUrl = "https://unpkg.com/${libName}@${libVersion}"

//开始处理
println "从: ${rootUrl} 开始下载..."

def enumFilesList = []
def unpkgFileList = getJsonFileDetails(rootUrl + "/")  //获取UNPKG的文件列表
if (unpkgFileList != null) {
  enumFilesList.addAll(unpkgFileList) // 1.先添加第1层的子节点到迭代列表里
}

/* JSON数据格式:
 {
 "path": "/package.json",
 "type": "file",
 "contentType": "application/json",
 "integrity": "sha384-XaJkaI/gjST68WtFzt/DMmwB6QxdOwY9uQA3SlN5cJ3zNFptgBtVThLaRVH85mxA",
 "size": 5619
 }
 或者
 {
 "path": "/src",
 "type": "directory"
 }
 */
def tempFile
while (!enumFilesList.isEmpty()) { // 2. 开始迭代
  tempFile = enumFilesList.pop() // 移除并返回列表的第一个元素
  if(tempFile.path.endsWith(".DS_Store")) {
    continue
  }

  if (tempFile.type.equals("directory")) {  //是目录
    File dirMk = new File(fileDownloadPath, tempFile.path)
    dirMk.mkdir()
    println "创建目录:" + dirMk.getAbsolutePath()

    unpkgFileList = getJsonFileDetails(rootUrl + tempFile.path + "/")  //获取UNPKG的文件列表
    if (unpkgFileList != null) {  // 3.有子节点则加入迭代列表
      enumFilesList.addAll(unpkgFileList)
    }
  } else {  //是文件
    try {
      byte[] byData = fetchBytes("${rootUrl}${tempFile.path}")
      if(byData !=null) {

        File fileMk = new File(fileDownloadPath, tempFile.path)
        fileMk.delete()
        fileMk.createNewFile()

        fileMk.append(byData)
        println "写入文件:" + fileMk.getAbsolutePath()
      }
    } catch (Exception e) {
      println e.getMessage()
    }
  }
}

println "从: ${rootUrl} 下载完成!"

//获取UNPKG的文件列表,JSON数组格式
/*
 [{
 "path": "/README.md",
 "type": "file",
 "contentType": "text/markdown",
 "integrity": "sha384-WHIETHDGbZAtsAin+SjjPlsRu637jsokH0C+IITYAViEhTL3IBtHm58eFzoPuQ/9",
 "size": 16853
 },
 {
 "path": "/lib",
 "type": "directory"
 }
 ]
 */

def getJsonFileDetails(String url) {
  String rData = fetchStr(url)

  def tsParser = new Parser()
  def xmlSlurper = new XmlSlurper(tsParser)

  def htmlDom = xmlSlurper.parseText(rData)

  String findData = "window.__DATA__ = {\"packageName\":\"${libName}\",\"packageVersion\":\"${libVersion}\""
  String sJson = htmlDom.head.script.find {
    if (it.text().startsWith(findData)) {
      return true
    }
  }
  sJson = sJson.substring(18)  //去掉 "window.__DATA__ = "

  def jsonSlurper = new JsonSlurper()
  def fileData = jsonSlurper.parseText(sJson)

  def result = []
  fileData.target.details.each {
    result.add(it.value)
  }
  return result
}

String fetchStr(String url) {
  int reTry = 0
  while (reTry < 3) {
    try {
      String rData = url.toURL().getText(["connectTimeout": 60 * 1000, "readTimeout": 60 * 1000, useCaches: false], "UTF-8")
      return rData
    } catch (Exception e) {
      println e.getMessage()
      reTry++
      Thread.sleep(3*1000)
      if (reTry >= 3) {
        println "发生错误,未能完成下载!${e.getMessage()}"
        System.exit(-1)
      }
    }
  }
}

byte[] fetchBytes(String url) {
  int reTry = 0
  while (reTry < 3) {
    try {
      byte[] rData = url.toURL().getBytes(["connectTimeout": 60 * 1000, "readTimeout": 60 * 1000, useCaches: false])
      return rData
    } catch (Exception e) {
      if(e.getMessage().startsWith("Server returned HTTP response code: 403 for URL")) {
        return null
      }

      println e.getMessage()
      reTry++
      Thread.sleep(3*1000)
      if (reTry >= 3) {
        println "发生错误,未能完成下载!${e.getMessage()}"
        System.exit(-1)
      }
    }
  }
}

/* UNPKG返回的数据格式,就是解析它来完成下载.
 {
 "packageName": "element-ui",
 "packageVersion": "2.13.2",
 "availableVersions": ["0.1.0", "0.1.1", "0.1.2", "0.1.3", "0.1.4", "0.1.5", "0.1.6", "0.1.7", "0.1.8", "0.1.9", "0.2.0", "0.2.1", "0.2.2", "0.2.3", "0.2.4", "0.2.5", "0.2.6", "1.0.0-rc.1", "1.0.0-rc.2", "1.0.0-rc.3", "1.0.0-rc.4", "1.0.0-rc.5", "1.0.0-rc.6", "1.0.0-rc.7", "1.0.0-rc.8", "1.0.0-rc.9", "1.0.0", "1.0.1", "1.0.2", "1.0.3", "1.0.4", "1.0.5", "1.0.6", "1.0.7", "1.0.8", "1.0.9", "1.1.0", "1.1.1", "1.1.2", "1.1.3", "1.1.4", "1.1.5", "1.1.6", "1.2.0", "1.2.1", "1.2.2", "1.2.3", "1.2.4", "1.2.5", "1.2.6", "1.2.7", "1.2.8", "1.2.9", "1.3.0-beta.2", "1.3.0-beta.3", "1.3.0", "1.3.1", "1.3.2", "1.3.3", "1.3.4", "1.3.5", "1.3.6", "1.3.7", "1.4.0-beta.1", "1.4.0", "1.4.1", "1.4.2", "1.4.3", "1.4.4", "1.4.5", "1.4.6", "1.4.7", "1.4.8", "1.4.9", "1.4.10", "1.4.11", "1.4.12", "1.4.13", "2.0.0-alpha.1", "2.0.0-alpha.2", "2.0.0-alpha.3", "2.0.0-beta.1", "2.0.0-rc.1", "2.0.0", "2.0.1", "2.0.2", "2.0.3", "2.0.4", "2.0.5", "2.0.6", "2.0.7", "2.0.8", "2.0.9", "2.0.10", "2.0.11", "2.1.0", "2.2.0", "2.2.1", "2.2.2", "2.3.0", "2.3.1", "2.3.2", "2.3.3", "2.3.4", "2.3.5", "2.3.6", "2.3.7", "2.3.8", "2.3.9", "2.4.0", "2.4.1", "2.4.2", "2.4.3", "2.4.4", "2.4.5", "2.4.6", "2.4.7", "2.4.8", "2.4.9", "2.4.10", "2.4.11", "2.5.0", "2.5.1", "2.5.2", "2.5.3", "2.5.4", "2.6.0", "2.6.1", "2.6.2", "2.6.3", "2.7.0", "2.7.1", "2.7.2", "2.8.0", "2.8.1", "2.8.2", "2.9.0", "2.9.1", "2.9.2", "2.10.0", "2.10.1", "2.11.0", "2.11.1", "2.12.0", "2.13.0", "2.13.1", "2.13.2"],
 "filename": "/",
 "target": {
 "path": "/",
 "type": "directory",
 "details": {
 "/package.json": {
 "path": "/package.json",
 "type": "file",
 "contentType": "application/json",
 "integrity": "sha384-XaJkaI/gjST68WtFzt/DMmwB6QxdOwY9uQA3SlN5cJ3zNFptgBtVThLaRVH85mxA",
 "size": 5619
 },
 "/CHANGELOG.en-US.md": {
 "path": "/CHANGELOG.en-US.md",
 "type": "file",
 "contentType": "text/markdown",
 "integrity": "sha384-zWdwbXOXTIoITTrZw9qzqVxedGSLIV96VnX/4GHr0TWzl3VSLSfbn3euVzY40GXT",
 "size": 65405
 },
 "/CHANGELOG.es.md": {
 "path": "/CHANGELOG.es.md",
 "type": "file",
 "contentType": "text/markdown",
 "integrity": "sha384-EAEuRGOwUHKD2mN0QxxWAQjVB87brBVFDLkvdoEnI5d4n+4fCpRr3c7bfXmPZfNs",
 "size": 77430
 },
 "/CHANGELOG.fr-FR.md": {
 "path": "/CHANGELOG.fr-FR.md",
 "type": "file",
 "contentType": "text/markdown",
 "integrity": "sha384-WF6AuoNtbbgP+3PlGv8flDrshJ2I2tlWRmxabhl6q7psBsMtvoxve1mN05K4CCuu",
 "size": 78562
 },
 "/CHANGELOG.zh-CN.md": {
 "path": "/CHANGELOG.zh-CN.md",
 "type": "file",
 "contentType": "text/markdown",
 "integrity": "sha384-3qLwzibZ2jSaamHM1h2LvluokqMXn6vKooVezoxBMOYsiW2qx9sVzE+Z++vQ9jJx",
 "size": 66438
 },
 "/LICENSE": {
 "path": "/LICENSE",
 "type": "file",
 "contentType": "text/plain",
 "integrity": "sha384-pZsGnkMCnbeTVGGxN5pKD3POwdc3OE+JQkdIV3WPceZcwXyzcZqtygaa4p1zUPTK",
 "size": 1082
 },
 "/README.md": {
 "path": "/README.md",
 "type": "file",
 "contentType": "text/markdown",
 "integrity": "sha384-WHIETHDGbZAtsAin+SjjPlsRu637jsokH0C+IITYAViEhTL3IBtHm58eFzoPuQ/9",
 "size": 16853
 },
 "/lib": {
 "path": "/lib",
 "type": "directory"
 },
 "/packages": {
 "path": "/packages",
 "type": "directory"
 },
 "/src": {
 "path": "/src",
 "type": "directory"
 },
 "/types": {
 "path": "/types",
 "type": "directory"
 }
 }
 }
 }
 */
