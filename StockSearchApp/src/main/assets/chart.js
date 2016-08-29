
            var Markit = {};
            /**
             * Define the InteractiveChartApi.
             * First argument is symbol (string) for the quote. Examples: AAPL, MSFT, JNJ, GOOG.
             * Second argument is duration (int) for how many days of history to retrieve.
             */
            Markit.InteractiveChartApi = function(symbol,duration){
                this.symbol = symbol;
                this.duration = duration;
                this.PlotChart();
            };


            Markit.InteractiveChartApi.prototype._fixDate = function(dateIn) {
            var dat = new Date(dateIn);
            return Date.UTC(dat.getFullYear(), dat.getMonth(), dat.getDate());
            };

            Markit.InteractiveChartApi.prototype._getOHLC = function(json) {
                var dates = json.Dates || [];
                var elements = json.Elements || [];
                var chartSeries = [];

                if (elements[0]){

                    for (var i = 0, datLen = dates.length; i < datLen; i++) {
                        var dat = this._fixDate( dates[i] );
                        var pointData = [
                            dat,
                            elements[0].DataSeries['open'].values[i],
                            elements[0].DataSeries['high'].values[i],
                            elements[0].DataSeries['low'].values[i],
                            elements[0].DataSeries['close'].values[i]
                        ];
                        chartSeries.push( pointData );
                    };
                }
                return chartSeries;
            };

           Markit.InteractiveChartApi.prototype.render = function(data) {
            // split the data set into ohlc and volume
            var ohlc = this._getOHLC(data),

            // set the allowed units for data grouping
            groupingUnits = [[
                'week',                         // unit name
                [1]                             // allowed multiples
            ], [
                'month',
                [1, 3, 6]
            ]];

            // create the chart
           $('#chartDisplay').highcharts('StockChart', {
                rangeSelector: {
                    selected: 0,
                    allButtonsEnabled: true,
                    buttons:[
                        {
                            type: 'week',
                            count: 1,
                            text: '1w'
                        },
                        {
                            type: 'month',
                            count: 1,
                            text: '1m'
                        },
                        {
                            type: 'month',
                            count: 3,
                            text: '3m'
                        },
                        {
                            type: 'month',
                            count: 6,
                            text: '6m'
                        },
                        {
                            type: 'year',
                            count: 1,
                            text: '1y'
                        },
                        {
                            type: 'ytd',
                            text: 'YTD'
                        },
                        {
                            type: 'All',
                            text: 'All'
                        },
                    ],
                    inputEnabled: false
                },
                exporting :
                {
                   enabled : false
                },
                plotOptions: {
                area: {
                    fillColor: {
                        linearGradient: { x1: 0, y1: 0, x2: 0, y2: 1},
                        stops: [
                            [0, Highcharts.getOptions().colors[0]],
                            [1, Highcharts.Color(Highcharts.getOptions().colors[0]).setOpacity(0).get('rgba')]
                        ]
                    }
                 }
                },

                title: {
                    text: this.symbol + ' Stock Value'
                },

                yAxis: [{
                    title: {
                        text: 'Stock Value'
                    },
                    height: 200,
                    lineWidth: 2
                }],
                tooltip: {
                    valuePrefix: '$'
                },
                series: [{
                    type: 'area',
                    width: '100%',
                    name: this.symbol,
                    data: ohlc,
                    dataGrouping: {
                        units: groupingUnits
                    }
                }],
                credits: {
                    enabled:false
                }
            });
         };

            Markit.InteractiveChartApi.prototype.PlotChart = function(){
                var params = {
                    parameters: JSON.stringify( this.getInputParams() )
                }

                //Make JSON request for timeseries data
                $.ajax({
                    data: params,
                    url: "http://stock-market-search-1272.appspot.com/",
                    dataType: "json",
                    type: 'GET',
                    context: this,
                    success: function(json){
                        if (!json || json.Message)
                        {
                            console.error("Error: ", json.Message);
                            return;
                        }
                        this.render(json);
                    },
                    error: function(response,txtStatus){
                        console.log(response,txtStatus)
                    }
                });
            };

            Markit.InteractiveChartApi.prototype.getInputParams = function(){
                return {
                    Normalized: false,
                    NumberOfDays: this.duration,
                    DataPeriod: "Day",
                    Elements: [
                        {
                            Symbol: this.symbol,
                            Type: "price",
                            Params: ["ohlc"]
                        }
                    ]
                }
            };

            function display(name)
            {
                new Markit.InteractiveChartApi(name,1095);

            }