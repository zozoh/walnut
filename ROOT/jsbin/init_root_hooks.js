sys.exec("touch /sys/hook/write/create_thumbnail");
sys.exec('obj /sys/hook/write/create_thumbnail -u \'hook_by:[{mime:"^image/",ph:"!^/home/.+/(.thumbnail/gen|.publish/gen|www)"}]\'');
sys.exec('echo \'iimg id:\${id} -thumb 64x64 -Q\' > /sys/hook/write/create_thumbnail');

sys.exec("touch /sys/hook/write/create_video_preview");
sys.exec('obj /sys/hook/write/create_video_preview -u \'hook_by:[{mime:"^video/",ph:"!^/home/.+/(.thumbnail/gen|.publish/gen|www)"}]\'');
sys.exec('echo \'videoc id:\${id} -mode "preview_image"\' > /sys/hook/write/create_video_preview');