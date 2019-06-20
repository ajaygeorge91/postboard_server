$(function () {


    function readURL(input) {
        if (input.files && input.files[0]) {
            var reader = new FileReader();

            reader.onload = function (e) {
                $('#image_upload_preview').attr('src', e.target.result);
            };

            reader.readAsDataURL(input.files[0]);
        }
    }

    $("#inputFile").on('change', function () {
        readURL(this);
    });

    $('#add_post_form').submit(function(event) {
        if (!validate()) {
            event.preventDefault();
            alert("Add a content or image");
        }
    });

    function validate() {
        var cval = $("#content").val();
        var ival = $("#inputFile").val();
        if (cval === '' && ival === '') {
            return false;
        }else{
            return true;
        }
    }


});