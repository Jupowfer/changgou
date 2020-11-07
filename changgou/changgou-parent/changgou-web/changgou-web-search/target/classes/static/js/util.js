function getQueryString(name){
    let reg = new RegExp("(^|&)"+ name +"=([^&]*)(&|$)");
    let r = window.location.search.substr(1).match(reg);
    if(r!=null)return unescape(r[2]); return null;
}
function getUrl(url){
    // var url='http://dev.qingcheng.web-manager-java.itcast.cn'
    var url='http://seckill.changgou.com'
    return url
}
//实现深拷贝
function copyDeep(templateData) {
  // templateData 是要复制的数组或对象，这样的数组或者对象就是指向新的地址的
  return JSON.parse(JSON.stringify(templateData));
}

//将数据转换为 JavaScript 对象
function jsonParse(data) {
    // templateData 是要复制的数组或对象，这样的数组或者对象就是指向新的地址的
    return JSON.parse(data);
  }
// 
function getUrlKey(name){
    return decodeURIComponent((new RegExp('[?|&]' + name + '=' + '([^&;]+?)(&|#|;|$)').exec(location.href) || [, ""])[1].replace(/\+/g, '%80')) || null
}
function unique1(arr){
    var hash=[];
    for (var i = 0; i < arr.length; i++) {
       if(hash.indexOf(arr[i])==-1){
        hash.push(arr[i]);
       }
    }
    return hash;
  }