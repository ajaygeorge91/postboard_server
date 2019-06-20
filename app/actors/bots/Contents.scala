package actors.bots

import scala.util.Random

/**
  * Created by Android SD-1 on 25-03-2017.
  */
object Contents {

  final val loremArray = Array("fermentum","\r\n","\t","\r\n\r\n\r\n", "eu", "vitae", "nisl", "elit", "porttitor", "pretium", "pellentesque", "mattis", "integer", "taciti", "vulputate", "parturient", "pretium", "interdum", "vulputate", "netus", "posuere", "elementum", "torquent", "a", "ad", "a", "suscipit", "gravida", "condimentum", "consectetuer", "sapien", "Molestie", "primis", "placerat", "hac", "magna", "tempus", "Magna", "cubilia", "nullam", "sollicitudin", "quisque", "nascetur", "tellus", "Dictumst", "rutrum", "cum", "sapien", "lorem", "pulvinar", "a", "arcu", "iaculis", "bibendum", "Quis", "montes", "quis", "vivamus", "lacus", "potenti", "integer", "felis", "pharetra", "hymenaeos", "id", "pharetra", "Etiam", "suspendisse", "donec", "mauris", "mattis", "mus", "etiam", "cursus", "Est", "lobortis", "eleifend", "sapien", "mi", "vitae", "class", "felis", "nam", "primis", "semper", "hac", "torquent", "netus", "placerat", "et", "Parturient", "per", "dictum", "quis", "tortor", "sem", "vivamus", "inceptos", "dignissim", "molestie", "tempus", "metus", "mollis", "interdum", "eget", "ante", "pharetra", "cursus", "ac", "et", "dictum", "feugiat", "#eleifend", "#Sollicitudin", "#adipiscing", "#Hendrerit", "#sem", "#tortor", "#sagittis", "#Odio", "#accumsan", "#quis", "#Lorem", "#class", "#primis", "#sapien", "#fringilla", "#lectus")
  final val loremArrayContent = Array("lorem", "Mollis","\r\n","\t","\r\n\r\n\r\n", "Penatibus", "nonummy", "semper", "cum", "a", "Sem", "pellentesque", "dapibus", "natoque", "fermentum", "eu", "vitae", "nisl", "elit", "porttitor", "pretium", "pellentesque", "mattis", "integer", "taciti", "vulputate", "parturient", "pretium", "interdum", "vulputate", "netus", "posuere", "elementum", "torquent", "a", "ad", "a", "suscipit", "gravida", "condimentum", "consectetuer", "sapien", "Molestie", "primis", "placerat", "hac", "magna", "tempus", "Magna", "cubilia", "nullam", "sollicitudin", "quisque", "nascetur", "tellus", "Dictumst", "rutrum", "cum", "sapien", "lorem", "pulvinar", "a", "arcu", "iaculis", "bibendum", "Quis", "montes", "quis", "vivamus", "lacus", "potenti", "integer", "felis", "pharetra", "hymenaeos", "id", "pharetra", "Etiam", "suspendisse", "donec", "mauris", "mattis", "mus", "etiam", "cursus", "Est", "lobortis", "eleifend", "sapien", "mi", "vitae", "class", "felis", "nam", "primis", "semper", "hac", "torquent", "netus", "placerat", "et", "Parturient", "per", "dictum", "quis", "tortor", "lorem", "potenti", "Ut", "Metus", "molestie", "Mus", "lorem", "sem", "vivamus", "inceptos", "dignissim", "molestie", "tempus", "metus", "mollis", "interdum", "eget", "ante", "pharetra", "cursus", "ac", "et", "dictum", "feugiat", "eu", "nisl", "condimentum", "per", "a", "Varius", "fermentum", "nascetur", "taciti", "mus", "semper", "class", "etiam", "litora", "congue", "nunc", "phasellus", "varius", "condimentum", "Cubilia", "dis", "amet", "nec", "porta", "Scelerisque", "tellus", "varius", "cum", "pede", "taciti", "leo", "Amet", "felis", "sociis", "per", "est", "dui", "purus", "litora", "Mattis", "Pretium", "Penatibus", "scelerisque", "laoreet", "mattis", "nonummy", "etiam", "augue", "nisl", "ultricies", "ultricies", "Ornare", "pretium")
  final val loremArrayMal = Array("ദുബായ്","\r\n","\t","\r\n\r\n\r\n", "എന്ന", "മഹാനഗരം", "ലോകത്തെ", "ഏറ്റവും", "സുരക്ഷിതമായ", "നഗരമായി", "നില", "നില്‍ക്കുന്നതിന്റെ ", "കാരണങ്ങളില്‍", "ഒന്നാണ്", "നിങ്ങള്‍", "ഈ", "ചിത്രങ്ങളില്‍", "#കണ്ടു", "#കൊണ്ടിരിക്കുന്നത്", "#റോഡ്‌", "#സുരക്ഷയില്‍", "#ഇത്രയധികം ", "#ശ്രദ്ധിക്കുന്ന", "#മറ്റൊരു", "#മിഡില്‍", "#ഈസ്റ്റ്", "#രാജ്യം", "#വേറെ", "കാണില്ല", "അല്‍", "അറബിയ്യ", "ചാനല്‍", "ആണ്", "കഴിഞ്ഞ", "ദിവസം", "ഈ", "ചിത്രങ്ങള്‍", "പുറത്ത്", "വിട്ടത്.", "ചാനലിന്", "പ്രത്യേകമായി", "ദുബായ്", "പോലീസിന്റെ", "ഈ", "ഓപറേഷന്‍", "മുറിയിലേക്ക്", "പ്രവേശനം", "ലഭിക്കുകയായിരുന്നു", "ദുബായ്", "നഗരത്തില്‍", "വസിക്കുന്ന", "ഓരോ", "പൌരന്റെയും", "പ്രൈവസിയെ", "ബാധിക്കാത്ത", "തരത്തില്‍", "എന്നാല്‍", "ഓരോരുത്തര്‍ക്കും", "സുരക്ഷയായിട്ടാണ്", "ഈ", "ക്യാമറ", "വിന്ന്യാസം", "ദുബായ്", "പോലിസ്", "സാധ്യമാക്കിയത്.", "ഈ", "ക്യാമറകള്‍", "നിരീക്ഷിക്കുവാനായി", "പ്രത്യേകം", "പോലിസ്", "അംഗങ്ങള്‍", "തന്നെ", "ഓപറേഷന്‍", "മുറിയില്‍", "ഇരുന്നു", "പ്രവര്‍ത്തിക്കുന്നുണ്ട്")
  final val loremArrayMalContent = Array("സുരക്ഷിതത്വം","\r\n","\t","\r\n\r\n\r\n", "ഇല്ലായ്മയും", "അഡിക്ഷന്‍", "ഉണ്ടാക്കും", "എന്നതും", "മാതാപിതാക്കളെ", "കുഞ്ഞുങ്ങള്‍", "ഇന്റര്‍നെറ്റ്", "ഉപയോഗിക്കുന്നതില്‍", "നിന്നും", "തടയാറുണ്ട്.", "പക്ഷെ", "കാര്യം", "അങ്ങിനെയല്ല", "എന്നാണ്", "പുതിയ", "പഠനങ്ങള്‍", "കാണിക്കുന്നത്.", "ഓക്സ്ഫോര്‍ഡ്", "യൂണിവേഴ്സിറ്റി", "ഡിപ്പാര്‍ട്ട്മെന്റ്", "ഓഫ്", "എജുക്കേഷന്‍", "നടത്തിയ", "പഠനത്തിലാണ്", "ഇന്റര്‍നെറ്റ്", "ഉപയോഗിക്കാത്ത", "കുഞ്ഞുങ്ങള്‍", "വിദ്യാഭ്യാസപരമായി", "പിന്നോക്കാവസ്ഥയില്‍", "ആണെന്ന്", "കണ്ടെത്തിയത്", "നാഷണല്‍", "ഓഫിസ്", "ഓഫ്", "സ്റ്റാറ്റിറ്റിക്സിന്റെ", "കണക്ക്", "പ്രകാരം ", "ബ്രിട്ടീഷ്‌", "വീടുകളില്‍", "കുട്ടികള്‍ക്ക്‌", "ഇന്റര്‍നെറ്റ്", "നിരോധിച്ചിട്ടുണ്ട്.", "ഇത്തരം", "കുഞ്ഞുങ്ങള്‍", "സ്കൂളിലെ", "അസ്സെയിന്‍മെന്റുകള്‍", "പൂര്‍ത്തിയാക്കാന്‍", "വളരെയധികം", "ബുദ്ധിമുട്ടുന്നതായി", "കാണുന്നതായും", "സോഷ്യല്‍", "ആക്റ്റിവിറ്റികളില്‍", "ഇവര്‍", "കുറച്ചു", "മാത്രമേ", "പങ്കെടുക്കുന്നുവുള്ളൂ", "എന്നും", "റിപ്പോര്‍ട്ടില്‍", "ഉണ്ട്", "റിപ്പോര്‍ട്ട്", "പ്രകാരം", "അമിതമായ", "ഇന്റര്‍നെറ്റ്", "ഉപയോഗം", "കാരണം", "വരുന്ന", "ബുദ്ധിമുട്ടുകളെ", "പോലെ", "കുറഞ്ഞ", "ഇന്റര്‍നെറ്റ്‌", "ഉപയോഗം", "കാരണവും", "പല", "ബുദ്ധിമുട്ടുകളും", "വരാറുണ്ട്.", "ഇത്തരക്കാര്‍ക്ക്", "മറ്റു", "വിദ്യാര്‍ത്ഥികളുടെ", "കൂടെ", "പഠനത്തില്‍", "ഓടിയെത്താന്‍", "ഒരിക്കലും", "സാധിക്കില്ലത്രേ", "നിങ്ങളെന്തു", "പറയുന്നു", "ഈ", "റിപ്പോര്‍ട്ടിനെ", "കുറിച്ച്?", "നിങ്ങളുടെ", "മക്കളെ", "നിങ്ങളെ", "ഇന്റര്‍നെറ്റ്", "ഉപയോഗിക്കുന്നതില്‍", "നിന്നും", "തടയാറുണ്ട്‌", "നോക്കത്താ", "ദൂരത്ത്", "മഞ്ഞു", "മൂടിക്കിടക്കുന്ന", "ഐസ്", "ലാന്‍ഡില്‍", "തിളച്ചു", "മറിയുന്ന", "നിലയില്‍", "ഒരു", "ഉഷ്ണജല", "തടാകം", "കേട്ടാല്‍", "അത്ഭുതം", "തോന്നും", "എന്നാല്‍", "സംഗതി", "സത്യമാണ്", "പ്രമുഖ", "ലാന്‍ഡ്സ്കേപ്പ്", "ഫോട്ടോഗ്രാഫര്‍", "ആയ", "അല്‍ബാന്‍", "ഹെണ്ട്രിക്സ്", "ആണ്", "ആ", "അല്ഭുതക്കാഴ്ചകള്‍", "നമുക്ക്", "മുന്‍പിലേക്ക്", "ഇട്ടു", "തരുന്നത്")

  def takeLoremRandom() = {
    Random.shuffle(loremArray.toList).take(Random.nextInt(26) + 2).mkString(" ")
  }

  def takeLoremRandomContent() = {
    Random.shuffle(loremArrayContent.toList).take(Random.nextInt(100) + 2).mkString(" ")
  }

  def takeLoremRandomContentComment() = {
    if (Random.nextInt(10) > 8) {
      Random.shuffle(loremArrayContent.toList).take(Random.nextInt(100) + 2).mkString(" ")
    } else {
      Random.shuffle(loremArrayContent.toList).take(Random.nextInt(26) + 2).mkString(" ")
    }
  }

  def takeLoremRandomMal() = {
    Random.shuffle(loremArrayMal.toList).take(Random.nextInt(26) + 2).mkString(" ")
  }

  def takeLoremRandomContentMal() = {
    Random.shuffle(loremArrayMalContent.toList).take(Random.nextInt(100) + 2).mkString(" ")
  }

  def takeLoremRandomContentMalComment() = {
    if (Random.nextInt(10) > 8) {
      Random.shuffle(loremArrayMalContent.toList).take(Random.nextInt(100) + 2).mkString(" ")
    } else {
      Random.shuffle(loremArrayMalContent.toList).take(Random.nextInt(26) + 2).mkString(" ")
    }
  }

}
