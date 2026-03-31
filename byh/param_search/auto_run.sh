
python train_net.py --output-dir output/param_search_first_train_3_25 --active_power_model-weights ../active_power/output/pagerank/master/model_final.pth --cpu_io_model-weights ../cpu_io/output/pagerank/master/model_final.pth

python train_net.py --output-dir output/param_search_second_train_3_25 --model-weights output/param_search_first_train_3_25/model_final.pth --active_power_model-weights ../active_power/output/pagerank/master/model_final.pth --cpu_io_model-weights ../cpu_io/output/pagerank/master/model_final.pth



python train_net.py --eval-only --output-dir output/param_search_eval --model-weights output/param_search_second_train_3_25/model_final.pth --active_power_model-weights ../active_power/output/pagerank/master/model_final.pth --cpu_io_model-weights ../cpu_io/output/pagerank/master/model_final.pth


