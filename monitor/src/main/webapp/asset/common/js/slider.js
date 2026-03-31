(function () {

    const menuData = [
        {
            label: 'Real-Time System',
            href: 'module/index/index.html',
            imgSrc: 'common/img/zhexian.png',
            subItems: [
                { href: 'module/cpu/cpuinfo.html', label: 'Computing' },
                { href: 'module/memory/memory.html', label: 'Storage Space' },
                { href: 'module/io/io.html', label: 'Read/Write' }
            ]
        },
        {
            label: 'Real-Time Cluster',
            href: 'module/node/node_list.html',
            imgSrc: 'common/img/yuanshuju.png',
            subItems: [
                { href: 'module/namenode/namenode.html', label: 'NameNode' },
                { href: 'module/datanode/DataNode.html', label: 'DataNode' },
                { href: 'module/yarn/yarn.html', label: 'YARN' },
                { href: 'module/application/running_app_list.html', label: 'Applications' }
            ]
        },
        {
            label: 'RL Optimizer',
            imgSrc: 'common/img/chart-relation.png',
            subItems: [
                { href: 'module/rl/analysis.html', label: 'Analysis View'},
                { href: 'module/rl/models.html', label: 'Models View'},
                { href: 'module/rl/training.html', label: 'Training View'}
            ]
        }
    ];

    function renderItem(item) {
        let imgHtml = item.imgSrc
            ? `<img src="${item.imgSrc}" style="width:18px;height:18px"/>&nbsp;`
            : '';

        let subHtml = '';
        if (item.subItems && item.subItems.length > 0) {
            subHtml = `<ol style="display:block;padding-left:12px;margin:0;">` +
                item.subItems.map(sub => `
                    <li>
                        <a href="${sub.href}">
                            <label class="folderTwo">${sub.label}</label>
                        </a>
                    </li>`).join('') +
                `</ol>`;
        }

        let titleHtml = item.href
            ? `<a href="${item.href}"><label class="folderOne foldertop" style="cursor:pointer;">${imgHtml}${item.label}</label></a>`
            : `<label class="folderOne foldertop">${imgHtml}${item.label}</label>`;

        return `<li>${titleHtml}${subHtml}</li>`;
    }

    const slider = `
        <div style="background:#f2f2f2;position:absolute;top:75px;width:220px;bottom:0px;">
            <ol class="tree" style="padding-left:8px;padding-top:2px;margin:0;">
                ${menuData.map(renderItem).join('')}
            </ol>
        </div>
    `;

    const sliderDiv = document.createElement('div');
    sliderDiv.innerHTML = slider;
    document.body.appendChild(sliderDiv);
})();
