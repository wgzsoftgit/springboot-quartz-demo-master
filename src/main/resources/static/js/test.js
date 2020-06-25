function handleGetParams(targetParams) {
        let params = {}
        let urlParamsStr = location.search.slice(1).split('&')
        let urlArray = urlParamsStr.forEach(e => {
            if(!e) {
            return
        }

        let [k,y] = e.split('=')
        params[k] = y
    })
        params = Object.assign(params, targetParams);
        console.log("&&&&%"+window.location.pathname +"()");
        console.log("&&&"+o2u(params));
        window.location.href = window.location.pathname + '?' + o2u(params)
    }

    function o2u(o) {
        let url = ''
        for (let key in o) {
            url += `&${key}=${o[key]}`
        }
        return url.slice(1)
    }

    $('#form-label a,#form-release a,#form-area a').click(function(e) {
    	console.log(e);
        //e.preventDefault()
    })

    $('#form-label a').click(function() {
    	console.log("$$"+$(this).text().trim());
        handleGetParams({
            label: $(this).text().trim()
        })
    })
    $('#form-release a').click(function() {
        handleGetParams({
            release: $(this).text().trim()
        })
    })
    $('#form-area a').click(function() {
        handleGetParams({
            area: $(this).text().trim()
        })
    })


    // label 分类
    // release 年
    // area 地区